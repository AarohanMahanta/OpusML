import os
import requests
import json
import numpy as np
import logging
import psycopg2
from dotenv import load_dotenv
import urllib.parse
import laion_clap
from typing import List, Dict, Any, Optional
from transformers import pipeline

load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

ARCHIVE_SEARCH_URL = "https://archive.org/advancedsearch.php"
ARCHIVE_METADATA_URL = "https://archive.org/metadata/"

class ClassicalMusicRecommender:
    def __init__(self):
        logger.info("Loading CLAP model...")
        self.model = laion_clap.CLAP_Module(enable_fusion=False)
        self.model.load_ckpt()
        logger.info("CLAP model loaded.")

        logger.info("Loading AI classifier for genre filtering...")
        self.genre_classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")
        logger.info("AI classifier loaded.")

        self.conn = psycopg2.connect(
            host=os.getenv('DB_HOST', 'localhost'),
            database=os.getenv('DB_NAME', 'music_db'),
            user=os.getenv('DB_USER', 'postgres'),
            password=os.getenv('DB_PASSWORD', 'House493'),
            port=os.getenv('DB_PORT', '5432')
        )
        logger.info("PostgreSQL connection established.")

    def download_from_internet_archive(self, composer: str, title: str, track_id: str, target_dir="/tmp") -> Optional[str]:
        q = f'title:("{title}" OR "{title.split(":")[0]}") AND creator:("{composer}" OR "{composer.split()[0]}")'
        params = {
            'q': q,
            'fl': 'identifier,title,creator',
            'rows': 10,
            'page': 1,
            'output': 'json'
        }
        try:
            logger.info("Searching Internet Archive for: %s - %s", composer, title)
            r = requests.get(ARCHIVE_SEARCH_URL, params=params, timeout=15)
            r.raise_for_status()
            hits = r.json().get('response', {}).get('docs', [])
            if not hits:
                logger.info("No results on Internet Archive for %s - %s", composer, title)
                return None

            for hit in hits:
                identifier = hit.get('identifier')
                if not identifier:
                    continue
                meta_r = requests.get(ARCHIVE_METADATA_URL + identifier, timeout=15)
                if meta_r.status_code != 200:
                    continue
                meta = meta_r.json()
                files = meta.get('files', [])
                candidates = []
                for f in files:
                    name = f.get('name', '')
                    format_ = f.get('format', '').lower()
                    if format_ in ('mp3', 'mpeg'):
                        candidates.append((3, name))
                    elif 'ogg' in format_:
                        candidates.append((2, name))
                    elif format_ in ('wav', 'flac', 'wave'):
                        candidates.append((1, name))
                    elif 'audio' in format_ or name.lower().endswith(('.mp3', '.ogg', '.wav', '.flac')):
                        candidates.append((0, name))
                if not candidates:
                    continue

                candidates.sort(reverse=True)
                file_name = candidates[0][1]
                download_url = f"https://archive.org/download/{identifier}/{urllib.parse.quote(file_name)}"
                logger.info("Downloading from Internet Archive: %s", download_url)

                local_name = f"{track_id}_{identifier}_{os.path.basename(file_name)}"
                local_path = os.path.join(target_dir, local_name)
                with requests.get(download_url, stream=True, timeout=30) as dl:
                    dl.raise_for_status()
                    with open(local_path, 'wb') as out:
                        for chunk in dl.iter_content(chunk_size=8192):
                            if chunk:
                                out.write(chunk)
                logger.info("Saved archive audio to %s", local_path)
                return local_path
            return None
        except Exception as e:
            logger.exception("Internet Archive search/download failed: %s", e)
            return None

    def extract_audio_embedding(self, audio_path: str) -> np.ndarray:
        try:
            if not audio_path or not os.path.exists(audio_path):
                embedding = np.random.rand(512).astype(np.float32)
                logger.warning("Audio missing, using random embedding")
                return embedding

            embedding = self.model.get_audio_embedding_from_filelist([audio_path], use_tensor=False)
            os.remove(audio_path)
            return embedding[0]
        except Exception as e:
            logger.error(f"Failed to extract embedding: {e}")
            return np.random.rand(512).astype(np.float32)

    def add_track(self, track_id: str, name: str, composer: str) -> bool:
        try:
            with self.conn.cursor() as cur:
                cur.execute("SELECT id FROM tracks WHERE spotify_id=%s", (track_id,))
                if cur.fetchone():
                    logger.info(f"Track {track_id} already exists.")
                    return True

            if not self.is_classical_track(name, composer):
                logger.info(f"Skipping non-classical track: {name} by {composer}")
                return False

            audio_path = self.download_from_internet_archive(composer, name, track_id)
            embedding = self.extract_audio_embedding(audio_path)

            with self.conn.cursor() as cur:
                cur.execute("""
                    INSERT INTO tracks (spotify_id, name, artist, audio_source)
                    VALUES (%s, %s, %s, 'open_dataset') RETURNING id
                """, (track_id, name, composer))
                track_db_id = cur.fetchone()[0]

                cur.execute("""
                    INSERT INTO track_embeddings (track_id, embedding_json)
                    VALUES (%s, %s)
                """, (track_db_id, json.dumps(embedding.tolist())))

            self.conn.commit()
            logger.info(f"Added track {name} by {composer}")
            return True
        except Exception as e:
            logger.error(f"Failed to add track: {e}")
            self.conn.rollback()
            return False

    def get_recommendations(self, query_track_id: str, top_k: int = 5) -> List[Dict[str, Any]]:
        try:
            with self.conn.cursor() as cur:
                cur.execute("""
                    SELECT te.embedding_json
                    FROM track_embeddings te
                    JOIN tracks t ON te.track_id=t.id
                    WHERE t.spotify_id=%s
                """, (query_track_id,))
                result = cur.fetchone()
                if not result:
                    raise ValueError(f"Track {query_track_id} not found.")

                query_embedding = np.array(json.loads(result[0]))

                cur.execute("""
                    SELECT t.spotify_id, t.name, t.artist, te.embedding_json
                    FROM track_embeddings te
                    JOIN tracks t ON te.track_id=t.id
                    WHERE t.spotify_id != %s
                """, (query_track_id,))

                recommendations = []
                for row in cur.fetchall():
                    track_id, name, composer, embedding_json = row
                    embedding = np.array(json.loads(embedding_json))
                    similarity = float(
                        np.dot(query_embedding, embedding)
                        / (np.linalg.norm(query_embedding) * np.linalg.norm(embedding))
                    )
                    recommendations.append({
                        "track_id": track_id,
                        "name": name,
                        "composer": composer,
                        "similarity_score": similarity
                    })

                recommendations.sort(key=lambda x: x["similarity_score"], reverse=True)
                return recommendations[:top_k]
        except Exception as e:
            logger.error(f"Recommendation failed: {e}")
            return []

    def is_classical_track(self, title: str, composer: str, description: str = "") -> bool:
        """
        Uses a zero-shot classifier to determine if a track is classical music.
        Returns True if classified as 'classical' with high confidence.
        """
        try:
            text = f"{title} by {composer}. {description}"
            result = self.genre_classifier(text, candidate_labels=["classical", "non-classical"])
            label = result["labels"][0]
            score = result["scores"][0]
            logger.info(f"AI genre prediction: {label} ({score:.2f}) for {title} by {composer}")
            return label == "classical" and score > 0.75
        except Exception as e:
            logger.error(f"Genre classification failed for {title}: {e}")
            return False
