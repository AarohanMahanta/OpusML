import React from "react";
import { motion } from "framer-motion";

const curatedTracks = [
  {
    id: "61YM5SkqqeUjIBL7It56cs",
    name: "Nocturne Op. 9 No. 2",
    artist: "Frédéric Chopin",
    albumArt: "https://i.scdn.co/image/ab67616d0000b273500c3804415f3911be8ec409",
  },
  {
    id: "7JNjUQKUQO3MWc0uFW5OlZ",
    name: "The Nutcracker: Pas de deux",
    artist: "Pyotr Ilyich Tchaikovsky",
    albumArt: "https://i.scdn.co/image/ab67616d0000b2739f4cf56469977dc7a8823eb1",
  },
  {
    id: "2xizRhme7pYeITbH1NLLGt",
    name: "Swan Lake: Scene",
    artist: "Pyotr Ilyich Tchaikovsky",
    albumArt: "https://i.scdn.co/image/ab67616d0000b2731d9c6602aa95abd8c5b146da",
  },
  {
    id: "0BWJNm4TrO6H3qgiCmDBjM",
    name: "Toccata and Fugue in D minor",
    artist: "Johann Sebastian Bach",
    albumArt: "https://i.scdn.co/image/ab67616d0000b273d46d75c2d65ac06cb3185c22",
  },
  {
    id: "2gYl8ElyQkeyAR89NREHTA",
    name: "Air (Suite No. 3 in D)",
    artist: "Johann Sebastian Bach",
    albumArt: "https://i.scdn.co/image/ab67616d0000b273e067ad77fc29bc353e69fd4e",
  },
  {
    id: "6Er8Fz6fuZNi5cvwQjv1ya",
    name: "Clair de Lune",
    artist: "Claude Debussy",
    albumArt: "https://i.scdn.co/image/ab67616d0000b273c21a36e9f0e28c8c60eb502a",
  },
  {
    id: "0ZN01wuIdn4iT8VBggkOMm",
    name: "Symphony No. 5 In C Minor",
    artist: "Ludwig van Beethoven",
    albumArt: "https://i.scdn.co/image/ab67616d0000b2738dd8211c6f6e49c9185e0c7d",
  },
  {
    id: "1DfGPEHxTYeaJpiNA4xUb5",
    name: "Für Elise",
    artist: "Ludwig van Beethoven",
    albumArt: "https://i.scdn.co/image/ab67616d0000b2738dd8211c6f6e49c9185e0c7d",
  },
  {
    id: "1IRqLBc1JAKIsLcOKwIMyY",
    name: "La Campanella",
    artist: "Niccolò Paganini",
    albumArt: "https://i.scdn.co/image/ab67616d0000b273cf64a6e2e68bebfdf2874eb2",
  },
];

export default function TrendingCarousel({ onSelect }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.8, delay: 0.2 }}
      style={{ 
        marginTop: 60,
        padding: "0 20px"
      }}
    >
      <motion.h2
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
        style={{
          textAlign: "center",
          marginBottom: 40,
          fontSize: "2.2rem",
          fontWeight: "700",
          background: "linear-gradient(135deg, #d1c0ddff, #dab5faff)",
          WebkitBackgroundClip: "text",
          WebkitTextFillColor: "transparent",
          backgroundClip: "text",
          letterSpacing: "-0.02em",
        }}
      >
        Featured Classical Works
      </motion.h2>

      <div
        style={{
          display: "flex",
          overflowX: "auto",
          gap: 24,
          padding: "20px 10px 40px 10px",
          scrollbarWidth: "none",
          msOverflowStyle: "none",
        }}
      >
        {curatedTracks.map((track, index) => (
          <motion.div
            key={track.id}
            initial={{ opacity: 0, x: 50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.6 + index * 0.1 }}
            whileHover={{ 
              scale: 1.05,
              y: -8
            }}
            whileTap={{ scale: 0.98 }}
            style={{
              background: "rgba(255, 255, 255, 0.05)",
              backdropFilter: "blur(20px)",
              borderRadius: 20,
              padding: 20,
              width: 240,
              minWidth: 240,
              cursor: "pointer",
              textAlign: "center",
              border: "1px solid rgba(255, 255, 255, 0.1)",
              boxShadow: "0 8px 32px rgba(0, 0, 0, 0.2)",
              position: "relative",
              overflow: "hidden",
            }}
            onClick={() => onSelect(track)}
          >
            {/* Hover effect line */}
            <motion.div
              whileHover={{ scaleX: 1 }}
              initial={{ scaleX: 0 }}
              style={{
                position: "absolute",
                top: 0,
                left: 0,
                right: 0,
                height: "3px",
                background: "linear-gradient(90deg, #ff0080, #ff4da6)",
                transformOrigin: "left",
              }}
            />
            
            {/* Album Art Container */}
            <motion.div
              whileHover={{ scale: 1.05 }}
              transition={{ type: "spring", stiffness: 300, damping: 20 }}
              style={{
                width: "100%",
                height: 200,
                borderRadius: 12,
                overflow: "hidden",
                marginBottom: 16,
                boxShadow: "0 8px 24px rgba(0, 0, 0, 0.3)",
              }}
            >
              <img
                src={track.albumArt}
                alt={track.name}
                style={{
                  width: "100%",
                  height: "100%",
                  objectFit: "cover",
                  display: "block",
                }}
              />
            </motion.div>

            {/* Track Info */}
            <div style={{ textAlign: "left" }}>
              <motion.div
                style={{
                  fontWeight: "600",
                  fontSize: "1rem",
                  marginBottom: 6,
                  color: "#fff",
                  lineHeight: "1.3",
                  display: "-webkit-box",
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: "vertical",
                  overflow: "hidden",
                }}
              >
                {track.name}
              </motion.div>
              <motion.div
                style={{
                  fontSize: "0.85rem",
                  color: "#b3b3b3",
                  fontWeight: "500",
                  lineHeight: "1.2",
                }}
              >
                {track.artist}
              </motion.div>
            </div>

            {/* Play button overlay on hover */}
            <motion.div
              initial={{ opacity: 0, scale: 0.8 }}
              whileHover={{ opacity: 1, scale: 1 }}
              style={{
                position: "absolute",
                top: "50%",
                left: "50%",
                transform: "translate(-50%, -50%)",
                background: "rgba(255, 255, 255, 0.9)",
                borderRadius: "50%",
                width: 60,
                height: 60,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                backdropFilter: "blur(10px)",
                border: "2px solid rgba(255, 255, 255, 0.2)",
              }}
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="white">
                <path d="M8 5v14l11-7z"/>
              </svg>
            </motion.div>
          </motion.div>
        ))}
      </div>

      {/* Scroll hint */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 1.2 }}
        style={{
          textAlign: "center",
          color: "#666",
          fontSize: "0.9rem",
          marginTop: 10,
          fontStyle: "italic",
        }}
      >
        ← Scroll to discover more →
      </motion.div>
    </motion.div>
  );
}