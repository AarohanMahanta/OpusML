-- Create tracks table
CREATE TABLE IF NOT EXISTS tracks (
                                      id SERIAL PRIMARY KEY,
                                      spotify_id VARCHAR(255) UNIQUE NOT NULL,
    name TEXT NOT NULL,
    artist TEXT NOT NULL,
    audio_source VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Create track_embeddings table
CREATE TABLE IF NOT EXISTS track_embeddings (
                                                id SERIAL PRIMARY KEY,
                                                track_id INTEGER REFERENCES tracks(id) ON DELETE CASCADE,
    embedding_json JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_tracks_spotify_id ON tracks(spotify_id);
CREATE INDEX IF NOT EXISTS idx_track_embeddings_track_id ON track_embeddings(track_id);