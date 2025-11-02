import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

export default function SearchBar({ onSearch }) {
  const [query, setQuery] = useState('');
  const [isFocused, setIsFocused] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (query.trim()) onSearch(query);
  };

  const clearInput = () => {
    setQuery('');
  };

  return (
    <motion.form 
      onSubmit={handleSubmit}
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      style={{ 
        display: 'flex', 
        alignItems: 'center',
        gap: '12px',
        position: 'relative',
        maxWidth: '500px',
      }}
    >
      {/* Search Icon */}
      <motion.div
        animate={{ 
          scale: isFocused ? 1.1 : 1,
          color: isFocused ? '#1db954' : '#b3b3b3'
        }}
        transition={{ duration: 0.2 }}
        style={{
          position: 'absolute',
          left: '16px',
          zIndex: 2,
        }}
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.3-4.3"/>
        </svg>
      </motion.div>

      {/* Search Input */}
      <motion.input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onFocus={() => setIsFocused(true)}
        onBlur={() => setIsFocused(false)}
        placeholder="Search for composers, pieces, or tracks..."
        style={{ 
          padding: '16px 48px 16px 48px', // More padding on right for clear button
          width: '100%',
          background: 'rgba(255, 255, 255, 0.08)',
          border: '1px solid rgba(255, 255, 255, 0.1)',
          borderRadius: '50px',
          color: 'white',
          fontSize: '1rem',
          fontWeight: '500',
          backdropFilter: 'blur(20px)',
          outline: 'none',
          transition: 'all 0.3s ease',
        }}
        animate={{
          borderColor: isFocused ? 'rgba(29, 185, 84, 0.5)' : 'rgba(255, 255, 255, 0.1)',
          background: isFocused ? 'rgba(255, 255, 255, 0.12)' : 'rgba(255, 255, 255, 0.08)',
          boxShadow: isFocused ? '0 0 0 3px rgba(29, 185, 84, 0.1)' : '0 2px 10px rgba(0, 0, 0, 0.1)',
        }}
        whileHover={{
          background: 'rgba(255, 255, 255, 0.12)',
          borderColor: 'rgba(255, 255, 255, 0.2)',
        }}
      />

      {/* Clear Button - positioned inside input but not overlapping */}
      <AnimatePresence>
        {query.trim() && (
          <motion.button
            type="button"
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.8 }}
            onClick={clearInput}
            whileHover={{ scale: 1.1, backgroundColor: 'rgba(255, 255, 255, 0.2)' }}
            whileTap={{ scale: 0.9 }}
            style={{
              position: 'absolute',
              right: '120px',
              background: 'rgba(255, 255, 255, 0.1)',
              border: 'none',
              borderRadius: '50%',
              width: '28px',
              height: '28px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              color: '#b3b3b3',
              fontSize: '16px',
              fontWeight: 'bold',
              zIndex: 2,
            }}
          >
            ×
          </motion.button>
        )}
      </AnimatePresence>

      {/* Search Button */}
      <AnimatePresence>
        <motion.button
          type="submit"
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          whileHover={{ 
            scale: 1.05,
            background: '#d3a2f5ff'
          }}
          whileTap={{ scale: 0.95 }}
          style={{ 
            padding: '12px 24px',
            background: '#d3a2f5ff',
            border: 'none',
            borderRadius: '50px',
            color: 'white',
            fontSize: '0.9rem',
            fontWeight: '600',
            cursor: 'pointer',
            whiteSpace: 'nowrap',
            backdropFilter: 'blur(20px)',
            boxShadow: '0 4px 20px rgba(151, 29, 185, 0.3)',
          }}
        >
          Search
        </motion.button>
      </AnimatePresence>
    </motion.form>
  );
}