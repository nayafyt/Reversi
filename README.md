# Reversi

A full-stack Reversi (Othello) game with an AI opponent using the minimax algorithm.

## Features

- Interactive web UI with React
- AI opponent with 3 difficulty levels (Easy, Medium, Hard)
- Play as Black or White
- Move hints, last-move highlight, and live score tracking
- AI evaluation based on corner control, mobility, stability, frontier strategy, wedge patterns, and more

## Project Structure

```
backend/           Spring Boot REST API (Java 17)
frontend/          React UI (esbuild)
docker-compose.yml Full-stack deployment
```

## Running Locally

**Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000` (frontend dev server).

## Running with Docker

```bash
docker compose up --build
```

Frontend at `http://localhost:3000`, backend API at `http://localhost:8080`.

## API

| Method | Endpoint         | Description          |
|--------|------------------|----------------------|
| POST   | /api/game/new    | Start a new game     |
| POST   | /api/game/move   | Make a player move   |
| GET    | /api/game/state  | Get current state    |

## Authors

Naya Fytali, Sara Mourelatou, Sofia Vergi
