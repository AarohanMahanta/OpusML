## [1.2.0] - 2025-10-19
### Changed
- Replaced internal gRPC service layer with REST endpoints.
- Removed .proto files, codegen, and gRPC plugins.
- Endpoints now use JSON over HTTP:
  - GET /search?query=...&limit=...
  - GET /similar?track_id=...&limit=...
  - GET /audio-features?track_id=...
- Internal logic calling Spotify and Python services remains unchanged.
- Easier integration and debugging with JSON payloads.
