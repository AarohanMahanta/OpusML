# ---- Stage 1: Build dependencies ----
FROM python:3.11-slim AS builder
WORKDIR /app

# Install system dependencies for audio processing
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    libgomp1 \
    libsndfile1 \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install into a separate prefix
COPY requirements.txt .
RUN pip install --prefix=/install --no-cache-dir -r requirements.txt

# ---- Stage 2: Copy code + dependencies into clean image ----
FROM python:3.11-slim
WORKDIR /app

# Copy installed Python packages
COPY --from=builder /install /usr/local

# Copy app code
COPY . .

# Expose Railway dynamic port
EXPOSE 8000
CMD ["sh", "-c", "uvicorn service:app --host 0.0.0.0 --port $PORT"]
