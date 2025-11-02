import React from "react";
import { motion } from "framer-motion";

export default function IntroScreen({ onFinish }) {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        background: "linear-gradient(135deg, #121212 0%, #1a1a1a 100%)",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        color: "white",
        fontFamily: "'Inter', sans-serif",
        zIndex: 1000,
      }}
    >
      {/* Animated Logo */}
      <motion.div
        initial={{ scale: 0, rotate: -180 }}
        animate={{ scale: 1, rotate: 0 }}
        transition={{ duration: 1, type: "spring" }}
        style={{
          width: "120px",
          height: "120px",
          background: "linear-gradient(135deg, #f396b8ff, #fcd9edff)",
          borderRadius: "24px",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          marginBottom: "40px",
          boxShadow: "0 20px 40px rgba(235, 83, 169, 0.3)",
        }}

      >
        <motion.span
          animate={{ scale: [1, 1.2, 1] }}
          transition={{ duration: 2, repeat: Infinity }}
          style={{ fontSize: "48px", color: "white" }}
        >
          ♫
        </motion.span>
      </motion.div>

      {/* Title */}
      <motion.h1
        initial={{ y: 50, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.5, duration: 0.8 }}
        style={{
          fontSize: "4rem",
          fontWeight: "800",
          background: "linear-gradient(135deg, #fff, #fcd9edff)",
          WebkitBackgroundClip: "text",
          WebkitTextFillColor: "transparent",
          backgroundClip: "text",
          margin: "0 0 20px 0",
          letterSpacing: "-0.02em",
        }}
      >
        OpusML
      </motion.h1>

      {/* Subtitle */}
      <motion.p
        initial={{ y: 30, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.8, duration: 0.8 }}
        style={{
          fontSize: "1.4rem",
          color: "#b3b3b3",
          margin: "0 0 50px 0",
          fontWeight: "500",
          textAlign: "center",
          maxWidth: "500px",
          lineHeight: "1.5",
        }}
      >
        Discover classical music through ML-powered recommendations
      </motion.p>

      {/* Get Started Button */}
      <motion.button
        initial={{ y: 30, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 1.2, duration: 0.6 }}
        whileHover={{ 
          scale: 1.05,
          background: "#ff87cbff",
          boxShadow: "0 10px 30px rgba(224, 100, 127, 0.4)"
        }}
        whileTap={{ scale: 0.95 }}
        onClick={onFinish}
        style={{
          padding: "18px 48px",
          fontSize: "1.2rem",
          background: "#ef7373ff",
          color: "white",
          border: "none",
          borderRadius: "30px",
          cursor: "pointer",
          fontWeight: "600",
          letterSpacing: "0.5px",
          transition: "all 0.3s ease",
        }}
      >
        Get Started
      </motion.button>

      {/* Loading dots */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 1.5 }}
        style={{
          display: "flex",
          gap: "8px",
          marginTop: "40px",
        }}
      >
        {[0, 1, 2].map((i) => (
          <motion.div
            key={i}
            animate={{
              scale: [1, 1.2, 1],
              opacity: [0.5, 1, 0.5],
            }}
            transition={{
              duration: 1.5,
              repeat: Infinity,
              delay: i * 0.2,
            }}
            style={{
              width: "8px",
              height: "8px",
              backgroundColor: "#fcd9edff",
              borderRadius: "50%",
            }}
          />
        ))}
      </motion.div>
    </motion.div>
  );
}