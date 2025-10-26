from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import logging
from clap_recommender import ClassicalMusicRecommender

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Classical Music Recommendation API")

# Initialize recommender
recommender = ClassicalMusicRecommender()
logger.info("ClassicalMusicRecommender initialized successfully!")


# -----------------------------
# Request / Response Models
# -----------------------------
class AddTrackRequest(BaseModel):
    track_id: str
    name: str
    composer: str


class RecommendationRequest(BaseModel):
    track_id: str
    top_k: int = 5


class RecommendationResponse(BaseModel):
    track_id: str
    name: str
    composer: str
    similarity_score: float


class HealthResponse(BaseModel):
    status: str
    track_count: int


class SyncTracksRequest(BaseModel):
    tracks: List[AddTrackRequest]


# -----------------------------
# Endpoints
# -----------------------------
@app.get("/")
async def root():
    return {"message": "Classical Music Recommendation API is running!"}


@app.get("/health")
async def health_check() -> HealthResponse:
    try:
        with recommender.conn.cursor() as cur:
            cur.execute("SELECT COUNT(*) FROM tracks")
            track_count = cur.fetchone()[0]
        return HealthResponse(status="healthy", track_count=track_count)
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return HealthResponse(status="unhealthy", track_count=0)


@app.post("/tracks", status_code=201)
async def add_track(request: AddTrackRequest):
    """
    Add a track to the database.
    """
    try:
        logger.info(f"Adding track: {request.name} by {request.composer}")
        success = recommender.add_track(
            track_id=request.track_id,
            name=request.name,
            composer=request.composer
        )
        if success:
            return {
                "status": "success",
                "message": f"Track '{request.name}' added successfully",
                "track_id": request.track_id
            }
        else:
            raise HTTPException(status_code=500, detail="Failed to add track")
    except Exception as e:
        logger.error(f"Error adding track: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/sync-tracks")
async def sync_tracks(request: SyncTracksRequest):
    """
    Bulk sync tracks - checks existence and only adds new ones
    """
    try:
        added_count = 0
        existing_count = 0

        # Batch check existing tracks
        track_ids = [t.track_id for t in request.tracks]
        if not track_ids:
            return {
                "status": "success",
                "added": 0,
                "existing": 0,
                "total_processed": 0
            }

        placeholders = ','.join(['%s'] * len(track_ids))

        with recommender.conn.cursor() as cur:
            cur.execute(f"""
                SELECT spotify_id FROM tracks 
                WHERE spotify_id IN ({placeholders})
            """, track_ids)
            existing_track_ids = {row[0] for row in cur.fetchall()}

        # Add only new tracks
        for track in request.tracks:
            if track.track_id not in existing_track_ids:
                success = recommender.add_track(
                    track_id=track.track_id,
                    name=track.name,
                    composer=track.composer
                )
                if success:
                    added_count += 1
            else:
                existing_count += 1

        return {
            "status": "success",
            "added": added_count,
            "existing": existing_count,
            "total_processed": len(request.tracks)
        }

    except Exception as e:
        logger.error(f"Bulk sync failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/recommend")
async def get_recommendations(request: RecommendationRequest) -> List[RecommendationResponse]:
    """
    Get similar tracks for a given track ID.
    """
    try:
        logger.info(f"Getting recommendations for track: {request.track_id}")
        recs = recommender.get_recommendations(request.track_id, top_k=request.top_k)
        return [
            RecommendationResponse(
                track_id=r['track_id'],
                name=r['name'],
                composer=r['composer'],
                similarity_score=r['similarity_score']
            )
            for r in recs
        ]
    except Exception as e:
        logger.error(f"Error getting recommendations: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/search")
async def search_tracks(query: str, limit: int = 5):
    """
    Search for tracks by composer or name.
    """
    try:
        with recommender.conn.cursor() as cur:
            cur.execute("""
                        SELECT spotify_id, name, artist
                        FROM tracks
                        WHERE LOWER(name) LIKE %s
                           OR LOWER(artist) LIKE %s
                            LIMIT %s
                        """, (f"%{query.lower()}%", f"%{query.lower()}%", limit))
            results = [
                {"track_id": r[0], "name": r[1], "composer": r[2]}
                for r in cur.fetchall()
            ]
        return results
    except Exception as e:
        logger.error(f"Search failed: {e}")
        raise HTTPException(status_code=500, detail="Database search failed")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="info")