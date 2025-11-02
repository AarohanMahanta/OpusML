import React from "react";

export default function TrackList({ tracks, onSelect, selectedTrack }) {
  return (
    <div className="track-list">
      {tracks.map((t) => (
        <div
          key={t.id}
          className={`track ${selectedTrack?.id === t.id ? "selected" : ""}`}
          onClick={() => onSelect(t)}
        >
          <img src={t.albumArt} alt={t.name} />
          <div className="track-info">
            <div className="track-name">{t.name}</div>
            <div className="track-artist">{t.artist}</div>
          </div>
        </div>
      ))}
    </div>
  );
}
