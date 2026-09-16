# AI Thumbnail Generator

An end-to-end system that lets users upload videos, extracts representative frames, scores each frame with an AI service, and returns the best thumbnails.

## Overview

This project has three main parts:

- **Backend** (`backend/`) — Spring Boot service that handles video uploads, frame extraction, job management, and coordination with the AI service.
- **AI Service** (`ai-service/`) — FastAPI service that scores images based on brightness, contrast, sharpness, colorfulness, and face presence.
- **Frontend** (`frontend/`) — Angular app for uploading videos and viewing the generated thumbnails.

Kafka is used to queue thumbnail jobs between the backend and the AI service, and MongoDB is used for persistence.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Angular 22, TypeScript, Angular Material |
| Backend | Java 21, Spring Boot 3.2, Spring Data MongoDB, Spring Kafka |
| AI Service | Python 3.9+, FastAPI, MediaPipe, Pillow, NumPy |
| Infra | Docker, Docker Compose, Kafka, Zookeeper, MongoDB |

## Project Structure

```
thumbnail-generator/
├── ai-service/          # FastAPI image scoring service
├── backend/             # Spring Boot backend
├── frontend/            # Angular frontend
├── docker-compose.yml   # Kafka, Zookeeper, Kafka UI
├── screenshots/         # UI screenshots for the README
├── .gitignore           # Build artifacts, venv, node_modules, uploads
└── README.md            # This file
```

## Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 20+ and npm
- Python 3.9+
- Docker and Docker Compose
- MongoDB (or use the Docker Compose setup once it is added)

## Quick Start

### 1. Start Kafka and supporting services

```bash
docker compose up -d
```

This starts Zookeeper, Kafka, and Kafka UI on http://localhost:8090.

### 2. Start the AI service

```bash
cd ai-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python main.py
```

The AI service runs on http://localhost:8000.

### 3. Start the backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend runs on http://localhost:8080.

### 4. Start the frontend

```bash
cd frontend
npm install
npm start
```

The Angular dev server runs on http://localhost:4200 by default.

## Screenshots

### Upload Form

![Upload Form](screenshots/upload-form.png)

### Style Selection

![Style Selection](screenshots/style-select.png)

### Resolution Selection

![Resolution Selection](screenshots/resolution-select.png)

### Results

![Results](screenshots/results.png)

## Configuration

### Backend

Edit `backend/src/main/resources/application.yml`:

```yaml
app:
  upload-dir: /path/to/uploads
  frames-dir: /path/to/frames
  ai-service:
    url: http://localhost:8000
  topic: thumbnail-jobs
```

### AI Service

The AI service can be configured via query parameters:

- `style` — scoring style. Options: `natural`, `bright`, `dark`, `colorful`, `sharp`, `face-focus`.

### Frontend

Update `frontend/src/app/core/environment/environment.ts` if your backend URL differs from the default.

## API Highlights

### Backend

- `POST /api/videos/upload` — Upload a video.
- `GET  /api/videos/{id}/status` — Check processing status.
- `GET  /api/videos/{id}/frames` — Get extracted frames and scores.

### AI Service

- `GET  /health` — Health check.
- `POST /score` — Score a single image.
- `POST /score/batch` — Score multiple images.

## Scoring Weights

The AI service combines the following features with style-specific weights:

- Sharpness
- Brightness
- Contrast
- Colorfulness
- Face presence

You can tune these in `ai-service/main.py`.

## Ignored Files

The `.gitignore` excludes build artifacts and local data:

- `target/`, `node_modules/`, `.venv/`
- `__pycache__/`, `.DS_Store`, `.idea/`, `.vscode/`
- `uploads/`, `frames/`, `thumbnails/`
- `.env` files

## TODO / Future Improvements

- [ ] Add MongoDB to `docker-compose.yml`
- [ ] Add tests for the backend, AI service, and frontend
- [ ] Add video preview in the frontend
- [ ] Support more scoring styles or custom weights
- [ ] Add CI/CD pipeline

## License

Add your license here.
