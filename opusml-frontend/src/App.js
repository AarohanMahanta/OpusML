import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Vibrant } from "node-vibrant/browser";
import TrackList from "./components/TrackList";
import SearchBar from "./components/SearchBar";
import IntroScreen from "./components/IntroScreen";
import TrendingCarousel from "./components/TrendingCarousel";
import { searchTracks, getRecommendations } from "./api";

function App() {
  const [loading, setLoading] = useState(false);
  const [tracks, setTracks] = useState([]);
  const [selectedTrack, setSelectedTrack] = useState(null);
  const [showIntro, setShowIntro] = useState(true);
  const [recommendations, setRecommendations] = useState([]);
  const [bgColor, setBgColor] = useState("#121212");




  const handleSearch = async (query) => {
    if (!query.trim()) return;

    setLoading(true);
    setSelectedTrack(null);
    setRecommendations([]);
    setBgColor("#121212");

    try {
      const response = await searchTracks(query);
      setTracks(response.data.tracks.slice(0, 5));
    } catch (err) {
      console.error(err);
      setTracks([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectTrack = async (track) => {
  setSelectedTrack(track);
  setTracks([]);
  setLoading(true);

  try {
    if (track.albumArt) {
      const palette = await Vibrant.from(track.albumArt).getPalette();
      const dominant =
        (palette.Vibrant && palette.Vibrant.hex) ||
        (palette.Muted && palette.Muted.hex) ||
        "#1db954";
      setBgColor(dominant);
    }

    const recs = await getRecommendations(track.id, 5); // now works
    setRecommendations(recs);
  } catch (err) {
    console.error(err);
    setRecommendations([]);
  } finally {
    setLoading(false);
  }
};


  const AlbumArt = ({ src, alt, style, className = "" }) => {
    if (!src) {
      return (
        <div 
          className={className}
          style={{
            ...style,
            background: "linear-gradient(135deg, #282828, #404040)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            color: "#b3b3b3",
            fontSize: "0.7em",
            fontWeight: "500",
          }}
        >
          <div style={{ transform: "rotate(-45deg)" }}>
            NO COVER
          </div>
        </div>
      );
    }
    

    return (
      <motion.div 
        className={className}
        style={{ ...style, overflow: "hidden" }}
        whileHover={{ scale: 1.02 }}
        transition={{ type: "spring", stiffness: 400, damping: 25 }}
      >
        <img 
          src={src} 
          alt={alt} 
          style={{ 
            width: "100%", 
            height: "100%", 
            objectFit: "cover",
            display: "block"
          }} 
        />
      </motion.div>
    );
  };

  if (showIntro) {
    return <IntroScreen onFinish={() => setShowIntro(false)} />;
  }

  return (
    <motion.div
      animate={{ backgroundColor: bgColor }}
      transition={{ duration: 1.5, ease: "easeInOut" }}
      style={{
        minHeight: "100vh",
        background: `linear-gradient(180deg, ${bgColor} 0%, #121212 400px)`,
        color: "white",
        fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
        overflowX: "hidden",
      }}
    >
      {/* Animated gradient background */}
      <div
        style={{
          position: "fixed",
          top: 0,
          left: 0,
          width: "100%",
          height: "100%",
          background: `
            radial-gradient(circle at 20% 80%, ${bgColor}40 0%, transparent 50%),
            radial-gradient(circle at 80% 20%, ${bgColor}30 0%, transparent 50%),
            radial-gradient(circle at 40% 40%, ${bgColor}20 0%, transparent 50%)
          `,
          zIndex: 0,
        }}
      />

      {/* Noise texture overlay */}
      <div
        style={{
          position: "fixed",
          top: 0,
          left: 0,
          width: "100%",
          height: "100%",
          backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 400 400' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='1' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.1'/%3E%3C/svg%3E")`,
          opacity: 0.03,
          zIndex: 1,
        }}
      />

      <div
        style={{
          position: "relative",
          zIndex: 2,
          padding: "32px 24px",
          maxWidth: "1400px",
          margin: "0 auto",
        }}
      >
 
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: "40px",
          }}
        >
          <motion.h1
            style={{
              fontSize: "2.5rem",
              fontWeight: "800",
              background: "linear-gradient(135deg, #fff 0%, #b3b3b3 100%)",
              WebkitBackgroundClip: "text",
              WebkitTextFillColor: "transparent",
              backgroundClip: "text",
              margin: 0,
              letterSpacing: "-0.02em",
            }}
            whileHover={{ scale: 1.02 }}
          >
            OpusML
          </motion.h1>
          <SearchBar onSearch={handleSearch} />
        </motion.div>

        {/* Content */}
        <div style={{ minHeight: "60vh" }}>
          {!selectedTrack && tracks.length === 0 && !loading && (
            <TrendingCarousel onSelect={handleSelectTrack} />
          )}

          {!selectedTrack && tracks.length > 0 && (
            <TrackList
              tracks={tracks}
              onSelect={handleSelectTrack}
              selectedTrack={selectedTrack}
            />
          )}

          <AnimatePresence>
            {selectedTrack && !loading && recommendations.length > 0 && (
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.5 }}
              >
                {/* Selected Track Header */}
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.2 }}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "24px",
                    marginBottom: "48px",
                    background: "rgba(255, 255, 255, 0.05)",
                    backdropFilter: "blur(20px)",
                    borderRadius: "16px",
                    padding: "24px",
                    border: "1px solid rgba(255, 255, 255, 0.1)",
                  }}
                >
                  <motion.div
                    whileHover={{ scale: 1.05, rotate: 2 }}
                    transition={{ type: "spring", stiffness: 400, damping: 25 }}
                  >
                    <AlbumArt
                      src={selectedTrack.albumArt}
                      alt={selectedTrack.name}
                      style={{
                        width: "120px",
                        height: "120px",
                        borderRadius: "12px",
                        boxShadow: "0 8px 32px rgba(0,0,0,0.3)",
                      }}
                    />
                  </motion.div>
                  <div style={{ textAlign: "left" }}>
                    <motion.h2
                      style={{
                        fontSize: "2rem",
                        fontWeight: "700",
                        margin: "0 0 8px 0",
                        background: "linear-gradient(135deg, #fff 0%, #b3b3b3 100%)",
                        WebkitBackgroundClip: "text",
                        WebkitTextFillColor: "transparent",
                        backgroundClip: "text",
                      }}
                    >
                      {selectedTrack.name}
                    </motion.h2>
                    <p style={{
                      color: "#b3b3b3",
                      fontSize: "1.1rem",
                      margin: "0 0 12px 0",
                      fontWeight: "500",
                    }}>
                      {selectedTrack.artist}
                    </p>
                    <motion.div
                      style={{
                        display: "inline-block",
                        background: "rgba(255, 255, 255, 0.1)",
                        padding: "8px 16px",
                        borderRadius: "20px",
                        fontSize: "0.9rem",
                        fontWeight: "600",
                        backdropFilter: "blur(10px)",
                      }}
                      whileHover={{ scale: 1.05, background: "rgba(255, 255, 255, 0.15)" }}
                    >
                      Similar Recommendations
                    </motion.div>
                  </div>
                </motion.div>

                {/* Recommendations Grid */}
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: 0.4 }}
                >
                  <h3 style={{
                    fontSize: "1.5rem",
                    fontWeight: "700",
                    marginBottom: "32px",
                    textAlign: "left",
                    color: "#fff",
                    letterSpacing: "-0.01em",
                  }}>
                    Similar Works
                  </h3>

                  <div style={{
                    display: "grid",
                    gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))",
                    gap: "24px",
                  }}>
                    {recommendations.map((r, i) => (
                      <motion.div
                        key={r.track_id || r.id || i}
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.4, delay: i * 0.1 }}
                        whileHover={{ y: -8 }}
                        style={{
                          background: "rgba(255, 255, 255, 0.05)",
                          backdropFilter: "blur(20px)",
                          borderRadius: "16px",
                          padding: "20px",
                          border: "1px solid rgba(255, 255, 255, 0.1)",
                          cursor: "pointer",
                          transition: "all 0.3s ease",
                          position: "relative",
                          overflow: "hidden",
                        }}
                      >
                        {/* Hover gradient overlay */}
                        <div style={{
                          position: "absolute",
                          top: 0,
                          left: 0,
                          right: 0,
                          height: "2px",
                          background: `linear-gradient(90deg, ${bgColor}, #1db954)`,
                          transform: "scaleX(0)",
                          transition: "transform 0.3s ease",
                        }} />
                        
                        <div style={{ display: "flex", gap: "16px", alignItems: "flex-start" }}>
                          <AlbumArt
                            src={r.albumArt}
                            alt={r.name}
                            style={{
                              width: "80px",
                              height: "80px",
                              borderRadius: "8px",
                              flexShrink: 0,
                            }}
                          />
                          
                          <div style={{ flex: 1, minWidth: 0, textAlign: "left" }}>
                            <h4 style={{
                              fontSize: "1rem",
                              fontWeight: "600",
                              margin: "0 0 6px 0",
                              color: "#fff",
                              whiteSpace: "nowrap",
                              overflow: "hidden",
                              textOverflow: "ellipsis",
                            }}>
                              {r.name}
                            </h4>
                            <p style={{
                              fontSize: "0.875rem",
                              color: "#b3b3b3",
                              margin: "0 0 12px 0",
                              fontWeight: "500",
                              whiteSpace: "nowrap",
                              overflow: "hidden",
                              textOverflow: "ellipsis",
                            }}>
                              {r.composer || r.artist}
                            </p>
                            
                            {r.similarity_score !== undefined && (
                              <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                                <div style={{
                                  flex: 1,
                                  height: "6px",
                                  background: "rgba(255, 255, 255, 0.1)",
                                  borderRadius: "3px",
                                  overflow: "hidden",
                                }}>
                                  <motion.div
                                    initial={{ width: 0 }}
                                    animate={{ width: `${r.similarity_score * 100}%` }}
                                    transition={{ duration: 0.8, delay: i * 0.1 + 0.2 }}
                                    style={{
                                      height: "100%",
                                      background: `linear-gradient(90deg, ${bgColor}, #1db954)`,
                                      borderRadius: "3px",
                                    }}
                                  />
                                </div>
                                <span style={{
                                  fontSize: "0.75rem",
                                  fontWeight: "600",
                                  color: "#1db954",
                                  minWidth: "40px",
                                }}>
                                  {(r.similarity_score * 100).toFixed(0)}%
                                </span>
                              </div>
                            )}
                          </div>
                        </div>
                      </motion.div>
                    ))}
                  </div>
                </motion.div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Footer */}
        <motion.div
          style={{
            marginTop: "80px",
            textAlign: "center",
            color: "#b3b3b3",
            fontSize: "0.875rem",
            padding: "24px",
            borderTop: "1px solid rgba(255, 255, 255, 0.1)",
          }}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1 }}
        >
          <p style={{ margin: 0, fontWeight: "500" }}>
            Discover classical music through ML-powered recommendations
          </p>
        </motion.div>
      </div>
    </motion.div>
  );
}

export default App;