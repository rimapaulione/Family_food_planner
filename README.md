# Family Food Shopping Planner

A collaborative family meal planning app where family members plan weekly meals together, manage recipes, and generate shopping lists automatically.

**Key features:**
- Recipe management with ingredients, categories, tags, and leftover chaining
- 2-week meal planning with drag-and-drop grid
- Auto-generated shopping lists from planned meals
- Family collaboration with invites and roles
- Smart recipe suggestions (seasonal, cook time, duplicates)

**Tech stack:** Spring Boot 3 (Java 21) + React 19 (TypeScript) + PostgreSQL + JWT Auth + Google OAuth2 (Spring Security)

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
