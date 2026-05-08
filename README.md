# Family Food Shopping Planner

> A collaborative weekly meal planner for families: build a recipe library, plan two weeks of meals together, auto-generate shopping lists, and let an AI help write new recipes.

**Live:** [https://forkplan.app](https://forkplan.app)

---

## What it does today

### Recipe library
- Per-family recipe collection: name, category, default servings, cooking time, tags, notes
- "Uses leftovers from" link between recipes — the planner uses this for routing
- Mark recipes as favorite
- Filter by category and search by name
- Inline ingredient management — fuzzy match prevents near-duplicates
- **AI-assisted recipe creation** (OpenAI `gpt-4o-mini`): write a Lithuanian sentence, get a structured recipe pre-filled into the form. Missing ingredients are auto-created on save.

### Meal plan (current week + next week)
- 7-day grid with breakfast / lunch / dinner slots
- Per-meal serving counts (override family defaults per slot)
- Lock a plan to freeze it for non-admins
- **"Plan my week" auto-fill** — score-based picker that:
  - Avoids recently-planned recipes (no-repeat window, configurable per family)
  - Caps total daily cooking time to family's setting (weekday + weekend separately)
  - Boosts kid-favorites, seasonal recipes, recently-added recipes
  - Routes leftovers: today's recipe → tomorrow's same meal-type slot
  - Top-3 random tiebreak so re-clicking gives variety
  - Skips slots where the family doesn't cook that meal (servings = null)
- **"Plan both weeks"** — admin-only desktop button, sequential auto-fill across weeks
- Daily cook-time warning indicator on the grid when total exceeds the family's cap

### Shopping list (per week)
- Computed on the fly from the meal plan — no stored list
- Aggregates ingredients per recipe with whole-batch multiplier
- Manual extra items section ("+ Add" — paper towels, milk, etc.)
- Mark plan items and manual items as bought; bought items survive plan changes ("ghost" rows)
- Tabs: This week / Next week
- Refreshes every 5s for collaborative shopping

### Family
- Create or join via invite link
- Invite by email → invitee registers / logs in → accepts
- Promote/demote members (admin role)
- **Remove members** (admin-only, with confirm)
- Family-level settings:
  - Default servings (weekday / weekend × breakfast / lunch / dinner)
  - Shopping day
  - "Don't repeat recipes within X days"
  - Total weekday cook time cap
  - Total weekend cook time cap

### Auth
- Local email + password (JWT)
- Spring Security with role-based authorization (USER, ADMIN)

### UI
- Responsive: separate desktop grid and mobile day-pager view for the planner
- Light/dark colors prepped via CSS variables (toggle TBD — see Roadmap)
- Tailwind v4 with semantic theme tokens

---

## Tech stack

| Layer | What |
|---|---|
| Backend | Spring Boot 3.4, Java 21, JPA / Hibernate, Spring Security, Flyway, MapStruct, Lombok |
| Database | PostgreSQL 17 |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS v4, TanStack Query, react-hook-form + Zod, react-router, lucide-react, sonner |
| AI | OpenAI `gpt-4o-mini` via REST + structured outputs (JSON schema) |
| Build / Dev | Maven (`mvnw`), npm, Docker, Docker Compose, `concurrently` (runs FE + BE together) |
| Deployment | AWS EC2, Docker Compose, custom domain via Cloudflare → forkplan.app |

---

## Database structure

![DB structure](docs/db_structure.png)

Migrations live in `planner_backend/src/main/resources/db/migration/` (Flyway, currently V1 → V13).

---

## Roadmap (planned)

Tracked in `notes/TODO.md` — short list of features deferred from the current scope:

- **Security**: invitation flow hardening (currently no email verification — see TODO for three options ranked by effort)
- **Drag and drop** on the planner (`@dnd-kit/core`)
- **Light / dark mode toggle** (CSS already supports it; need toggle UI + audit pass)
- **Re-plan whole week** (overwrite mode for auto-fill behind a confirm dialog)
- **Shopping list — don't double-count leftovers** (when recipe A and B share ingredients via leftover link, B's overlap shouldn't add)
- **AI rate limiter** (per-user hourly cap, currently wide open)
- **Quality**: extract `ScoringRule` strategy if auto-fill rules grow

Notes on architecture and prior phases live in `notes/` (Phase 9 — meal plan, Phase 10 — shopping list, Phase 11 — auto-fill, Phase 12 — OpenAI integration).

---

## Run locally

### Prerequisites
- Java 21
- Node.js 18+
- Docker Desktop (auto-started for the dev postgres)
- An OpenAI API key (only required if you want to test AI recipe generation — the rest works without it)

### One-command start

From the project root:

```bash
npm install
npm run dev
```

This launches both processes in parallel via `concurrently`:
- **Backend** at `http://localhost:8080`
  - Spring Boot's `spring-boot-docker-compose` integration auto-starts a postgres container from `planner_backend/compose.yaml` (port 5433)
  - Flyway runs migrations on startup
- **Frontend** at `http://localhost:5173` (Vite)

The backend's `application.yaml` reads sensible defaults; you usually don't need any env vars for dev.

### OpenAI key (optional, for AI recipe generation)

Create `planner_backend/src/main/resources/openai-secrets.yaml` (gitignored):

```yaml
openai:
  api-key: sk-proj-...
```

Spring Boot picks it up via `spring.config.import: optional:classpath:openai-secrets.yaml`. If absent, the AI feature returns a 401 from OpenAI when called — everything else still works.

### Tests

```bash
cd planner_backend && ./mvnw test
```

Frontend tests not yet present; type-check via `npx tsc -b --noEmit` from `planner_ui/`.

---

## Available scripts

From the project root:

| Command | What |
|---|---|
| `npm run dev` | Start backend + frontend together (with auto-managed postgres) |
| `npm run backend` | Start only Spring Boot |
| `npm run frontend` | Start only Vite dev server |
| `npm run build` | Production build of both |

---

## Project structure

```
.
├── docker-compose.prod.yml    # prod deployment (3 containers)
├── docs/
│   └── db_structure.png       # ER diagram (referenced above)
├── notes/                     # design docs and TODO list (gitignored)
│   ├── PHASE_9_PLAN.md
│   ├── PHASE_10_PLAN.md
│   ├── PHASE_11_PLAN.md
│   ├── PHASE_12_PLAN.md
│   └── TODO.md
├── planner_backend/
│   ├── Dockerfile             # multi-stage Java build
│   ├── compose.yaml           # local-dev postgres (auto-loaded by Spring)
│   ├── pom.xml
│   └── src/main/java/.../
│       ├── controller/        # REST endpoints
│       ├── service/           # business logic, including OpenAI + auto-fill
│       ├── repository/        # JPA repositories
│       ├── model/entity/      # JPA entities
│       ├── dto/               # request / response DTOs
│       ├── mapper/            # MapStruct entity ↔ DTO
│       ├── config/            # security, OpenAI client, CORS
│       └── ...
└── planner_ui/
    ├── Dockerfile             # frontend image (Vite build → nginx)
    └── src/
        ├── pages/             # route-level components
        ├── components/
        │   ├── layout/        # Header, Nav, MobileNav
        │   ├── recipes/       # recipe list, form, AI modal, ingredient picker
        │   ├── planner/       # meal plan grid, mobile view, slot edit modal, auto-fill action
        │   ├── shopping/      # shopping list, manual items, collapsible sections
        │   ├── family/        # family settings, members, invites
        │   ├── ingredients/   # ingredient library
        │   ├── auth/          # login + register cards/forms
        │   ├── user/          # profile + password
        │   └── ui/            # primitives: Button, Card, ConfirmDialog, ProgressBar, etc.
        ├── hooks/             # TanStack Query hooks per resource
        ├── stores/            # Zustand auth store
        ├── schemas/           # Zod schemas
        ├── types/             # shared TypeScript types
        ├── utils/             # text normalize, mealPlanHelpers, getErrorMessage
        ├── constants/         # categories, units, mealPlan
        └── api/               # axios instance with JWT interceptor
```

---

## Production deployment (brief)

- Single EC2 instance (Ubuntu)
- Three containers via `docker-compose.prod.yml`: postgres, backend, frontend (nginx)
- Secrets injected via root-level `.env` file (`DB_PASSWORD`, `JWT_SECRET`, `OPENAI_API_KEY`)
- HTTPS terminated by Cloudflare; instance only exposes port 8081 to Cloudflare's edge
- Manual deploy: SSH in, pull, `docker compose -f docker-compose.prod.yml up -d --build`
- CI/CD pending (planned: GitHub Actions → SSH deploy)
