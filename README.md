# Personal Tracker — Microservices

A personal finance + habit/task tracker, built as a real microservices system in Spring Boot. Multi-user: every user only ever sees their own data.

## Architecture

```
        ┌──────────────┐   ┌─────────────────┐   ┌────────────────┐
        │ Auth Service │   │ Expense Service │   │ Habit Service  │
        │    :8081     │   │      :8082      │   │     :8083      │
        │  auth_db     │   │   expense_db    │   │    habit_db    │
        └───────▲──────┘   └────────▲────────┘   └────────▲───────┘
                │                   │                      │
                └───────────────────┼──────────────────────┘
                                    │  static URLs (env vars)
                          ┌─────────┴────────┐
                          │   API Gateway     │  :8080  ← clients talk to this only
                          │ (JWT, CORS, rate  │
                          │     limiting)     │
                          └───────────────────┘
```

- **Auth Service** — register/login, issues JWTs (24h for users, 1yr for developer API keys). Owns `auth_db`.
- **Expense Service** — categories + expenses/income, monthly summaries. Owns `expense_db`.
- **Habit Service** — habits with streak tracking, plus one-off tasks. Owns `habit_db`.
- **API Gateway** — the only entrypoint clients should call. Routes to the right service by static URL (an env var per service), validates the JWT up front, applies rate limiting, handles CORS, and forwards the user's ID downstream as an `X-User-Id` header.

There's no service discovery (Eureka) — that was useful for local dev but is unnecessary complexity once deployed, since a real host (like Render) gives every service a stable address on its own. See `DEPLOY.md` for the production architecture, which additionally makes auth/expense/habit-service *private* (unreachable except through the gateway).

**Data ownership rule:** no service ever queries another service's database directly. Every entity just stores a `userId` (a UUID it trusts from the validated JWT) — there's no foreign key across service boundaries, because in a real deployment these databases could be on entirely different servers.

**Security:** the JWT is validated twice — once at the gateway (fast rejection of bad requests) and again inside each service (defense in depth, so a service is never left trusting the network alone).

## Prerequisites

- JDK 17+
- Maven 3.8+
- Docker (for Postgres) — or install Postgres locally and skip docker-compose

## Running it

**1. Start the databases**

```bash
docker compose up -d
```

This starts one Postgres container and auto-creates `auth_db`, `expense_db`, `habit_db` (see `init-db.sql`). Default credentials: `tracker` / `tracker`.

**2. Build everything**

```bash
mvn clean install
```

**3. Start each service** (each in its own terminal, or use a process manager). Order doesn't matter except the gateway is most useful started last:

```bash
cd auth-service    && mvn spring-boot:run
cd expense-service && mvn spring-boot:run
cd habit-service   && mvn spring-boot:run
cd api-gateway     && mvn spring-boot:run
```

All requests go through the gateway at **`http://localhost:8080`**.

## API quick reference

### Auth (public)
```
POST /api/auth/register   { "name", "email", "password" }  -> { token, userId, name, email }
POST /api/auth/login      { "email", "password" }           -> { token, userId, name, email }
```
Use the returned `token` as `Authorization: Bearer <token>` on every other request.

### Expenses (auth required)
```
GET    /api/categories
POST   /api/categories        { "name", "type", "icon" }
DELETE /api/categories/{id}

GET    /api/expenses
POST   /api/expenses          { "amount", "description", "categoryId", "type", "expenseDate" }
PUT    /api/expenses/{id}
DELETE /api/expenses/{id}
GET    /api/expenses/summary?year=2026&month=7   -> { totalIncome, totalExpense, netBalance }
```

### Habits & Tasks (auth required)
```
GET    /api/habits
POST   /api/habits            { "name", "frequency" }        # frequency: DAILY | WEEKLY
POST   /api/habits/{id}/complete                             # marks today done, updates streak
DELETE /api/habits/{id}

GET    /api/tasks
POST   /api/tasks             { "title", "description", "dueDate", "priority" }
PUT    /api/tasks/{id}
POST   /api/tasks/{id}/toggle                                 # flips completed on/off
DELETE /api/tasks/{id}
```

## Example flow (curl)

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Priya","email":"priya@example.com","password":"secret123"}'

# Copy the "token" from the response, then:
TOKEN="paste-token-here"

curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Groceries","type":"EXPENSE","icon":"🛒"}'

curl -X POST http://localhost:8080/api/habits \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Read 20 minutes","frequency":"DAILY"}'
```

## Deploying this publicly

See **`DEPLOY.md`** for a full Render deployment guide (one public gateway + three private services + shared Postgres, all from a single `render.yaml` Blueprint), and **`API_REFERENCE.md`** for the reference to hand to anyone building against your API — covers both end-user login and long-lived developer API keys.

## Notes / things to change before real production use

- `jwt.secret` in each `application.yml` is a dev-only default — set the `JWT_SECRET` env var to something long and random in real deployments, and use the same value across all services (Render prompts you for this, see `DEPLOY.md`).
- `ddl-auto: update` is convenient for development; switch to migrations (Flyway/Liquibase) before this goes anywhere real.
- Rate limiting and developer API keys are now in place (see `API_REFERENCE.md`) — still missing: refresh tokens, email verification, and true API-key revocation (current keys are stateless JWTs; see the tradeoffs section in `DEPLOY.md`).
- A `notification-service` (budget alerts, streak-about-to-break reminders) is a natural 5th service to add later — the architecture already supports it the same way habit-service was added.

## Project structure

```
personal-tracker/
├── pom.xml                  # parent POM - shared dependency versions
├── render.yaml               # Render Blueprint - one-shot deploy config
├── DEPLOY.md                 # Render deployment guide
├── API_REFERENCE.md          # public API docs for end users & third-party developers
├── render-deploy-setup.sql   # one-time SQL to add the 2nd/3rd database on Render
├── docker-compose.yml        # Postgres for local dev (all 3 databases)
├── init-db.sql
├── api-gateway/               # + Dockerfile
├── auth-service/              # + Dockerfile
├── expense-service/           # + Dockerfile
└── habit-service/             # + Dockerfile
```
