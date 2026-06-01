# Family Food Shopping Planner

A collaborative weekly meal planner for families. Plan meals, auto-generate shopping lists, and let AI help write recipes.

**Live:** [https://forkplan.app](https://forkplan.app)

---

## Tech stack

- **Backend:** Spring Boot 3.4, Java 21, PostgreSQL, Flyway
- **Frontend:** React 19, TypeScript, Vite, Tailwind v4
- **AI:** OpenAI `gpt-4o-mini`
- **Deploy:** Docker Compose on AWS EC2

---

## Run locally

**Prerequisites:** Java 21, Node 18+, Docker Desktop

```bash
npm install
npm run dev
```

- Backend: http://localhost:8080
- Frontend: http://localhost:5173

Spring auto-starts a Postgres container; defaults in `application.yaml` cover the rest — no `.env` needed.

### Optional: OpenAI key

Create `planner_backend/src/main/resources/openai-secrets.yaml` (gitignored):

```yaml
openai:
  api-key: sk-...
```

---

## Tests

```bash
cd planner_backend && ./mvnw test
```

---

## Project layout

- `planner_backend/` — Spring Boot API
- `planner_ui/` — React frontend
- `docs/` — DB diagram

---

## Production

Deployed to EC2 via `docker-compose.prod.yml`. Secrets live in `~/Family_food_planner/.env` on the server (never committed). HTTPS via Cloudflare.
