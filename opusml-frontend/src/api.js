import axios from "axios";

const API_BASE =
  process.env.REACT_APP_API_URL
    ? `${process.env.REACT_APP_API_URL}/api/spotify`
    : "http://localhost:8080/api/spotify";

// Search tracks
export const searchTracks = (query, limit = 10) => {
  return axios.get(`${API_BASE}/search`, { params: { query, limit } });
};

export const getRecommendations = (trackId, topK = 5) => {
  return axios
    .post(`${API_BASE}/recommend`, { trackId, topK })
    .then((res) =>
      Array.isArray(res.data.recommendations) ? res.data.recommendations : []
    )
    .catch((err) => {
      console.error("Error fetching recommendations:", err);
      return [];
    });
};
