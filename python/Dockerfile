FROM python:3.9-slim

WORKDIR /app

# Install system dependencies for PyTorch and audio processing
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    libgomp1 \
    libsndfile1 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Railway assigns a dynamic port via $PORT
EXPOSE 8000
CMD ["sh", "-c", "uvicorn service:app --host 0.0.0.0 --port $PORT"]
