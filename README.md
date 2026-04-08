# Family Food Shopping Planner

A full-stack application for planning family meals and shopping lists.

## Prerequisites

- Java 21
- Node.js 18+
- Docker (optional — not needed for local profile)

## Getting started

### Quick start (no Docker needed)

```bash
npm install
npm run dev:local
```

Uses an in-memory H2 database — no setup required.

### Full setup (with PostgreSQL)

```bash
npm install
npm run dev
```

Requires Docker. PostgreSQL starts automatically via Docker Compose.

Both options start the backend at http://localhost:8080 and frontend at http://localhost:5173.

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start with PostgreSQL (requires Docker) |
| `npm run dev:local` | Start with H2 in-memory database |
| `npm run backend` | Start Spring Boot backend only |
| `npm run frontend` | Start Vite frontend only |
| `npm run build` | Build both for production |
