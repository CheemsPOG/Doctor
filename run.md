# Running Doctor Ri Clinic

Three ways to run this app, from easiest to most flexible. Pick **Option A** unless you have a reason not to.

---

## Option A — Full stack with Docker (recommended)

Runs everything: MySQL, Redis, backend, frontend. No local JDK/Node needed.

**Step 1 — Open the project**

```powershell
cd "d:\Personal Projects\New\doctor\deploy"
```

**Step 2 — Create the env file**

```powershell
Copy-Item .env.example .env
```

Defaults are fine for local use. You only need to edit `.env` if a port below is already busy on your machine.

**Step 3 — Build and start**

```powershell
docker compose up -d --build
```

First run can take 10–20+ minutes (Maven + npm builds inside Docker). Wait until all containers are healthy.

**Step 4 — Watch startup (optional)**

```powershell
docker compose logs -f api
```

Wait until you see `Started ... Application` (Spring Boot ready). Press `Ctrl+C` to stop following logs (containers keep running).

**Step 5 — Verify backend health**

```powershell
curl http://localhost:8080/actuator/health
```

Expected: `{"status":"UP"}` (or similar).

**Step 6 — Open the app**

Go to: **http://localhost:8088**

**Stopping / cleaning up**

```powershell
docker compose down       # stop containers, keep data
docker compose down -v    # stop containers AND wipe MySQL/Redis data
```

---

## Option B — Backend only (Java, no Docker for the API)

Use this when you're actively editing backend code and want fast restarts. You still need MySQL + Redis running somewhere (easiest: start just those two via Docker, see below).

**Step 1 — Start MySQL + Redis only**

```powershell
cd "d:\Personal Projects\New\doctor\deploy"
docker compose up -d mysql redis
```

**Step 2 — Run the backend**

```powershell
cd "d:\Personal Projects\New\doctor\be-doc-doctor-ri"
# Requires JDK 20+
mvn test
mvn spring-boot:run
# or on a different port: SERVER_PORT=8081 mvn spring-boot:run
```

Backend comes up on **http://localhost:8080**. Health: http://localhost:8080/actuator/health

---

## Option C — Frontend only (Vite dev server)

Use this when you're actively editing frontend code and want hot reload. Needs a backend reachable at `:8080` (either Option A's Docker `api` container, or Option B).

```powershell
cd "d:\Personal Projects\New\doctor\fe-doc-doctor-ri"
npm install
npm run dev
```

Opens on **http://127.0.0.1:5173** — Vite proxies `/api` → `http://localhost:8080`.

Other useful commands in this folder:

```powershell
npm test              # Vitest
npm run build          # production build
npm run preview -- --host   # serve the build, reachable from other devices on your Wi-Fi (for PWA install testing)
```

---

## Demo accounts

All demo accounts use the same password: **`Password123!`**

| Email | Password | Role | Portal |
|-------|----------|------|--------|
| `mebau@doctorri.local` | `Password123!` | PATIENT | `/patient/*` — pregnant patient |
| `receptionist@doctorri.local` | `Password123!` | RECEPTIONIST | `/clinic/*` — front desk, queue, walk-in |
| `doctor.ri@doctorri.local` | `Password123!` | DOCTOR (DR-RI) | `/doctor/*` — schedule, queue, exams |
| `admin@doctorri.local` | `Password123!` | CLINIC_ADMIN | `/admin/*` — services, doctors, audit log |

These are seeded automatically on first boot (`shared/infrastructure/seed`), so they exist as soon as the backend starts against an empty database.

---

## URLs and ports

| Service | Container name | URL / host port | Notes |
|---------|-----------------|------------------|-------|
| App (frontend, via Nginx) | `web` | **http://localhost:8088** | Serves the React UI, proxies `/api` → `api` |
| Backend API (direct) | `api` | http://localhost:8080 | Same API Docker exposes; also reachable directly |
| Backend health check | `api` | http://localhost:8080/actuator/health | Used by Compose healthcheck too |
| MySQL | `mysql` | `localhost:3307` | DB name `db_clinic`, user `root` / `123456` (defaults) |
| Redis | `redis` | `localhost:6380` | Required — booking fails without it (slot hold) |
| Frontend dev server (Option C only) | — | http://127.0.0.1:5173 | Vite, proxies `/api` → `:8080` |

Ports are configurable via `.env` in `deploy/` — see `WEB_PUBLISH_PORT`, `API_PUBLISH_PORT`, `MYSQL_PUBLISH_PORT`, `REDIS_PUBLISH_PORT`.

**On the same Wi-Fi (e.g. testing on your phone):** since the frontend calls the API on the same origin (`/api/v1` → Nginx → `api:8080`), you only need:

```text
http://<your-computer-IP>:8088
```

---

## Useful commands

```powershell
# Follow backend logs
docker compose logs -f api

# Follow frontend (nginx) logs
docker compose logs -f web

# Check container health/status
docker compose ps

# Health check
curl -s http://localhost:8080/actuator/health

# Stop everything (keep data)
docker compose down

# Stop everything and wipe MySQL/Redis volumes
docker compose down -v

# Rebuild a single service after a code change
docker compose up -d --build api
docker compose up -d --build web
```

### Peek at the database while testing

Connect to MySQL on `localhost:3307` (user `root`, password `123456`, db `db_clinic`):

```sql
SELECT id, status, start_at, doctor_id FROM appointments ORDER BY id DESC LIMIT 5;
SELECT * FROM visit_queue ORDER BY id DESC LIMIT 5;
SELECT * FROM outbox_events ORDER BY id DESC LIMIT 5;
```

---

## Notes / troubleshooting

- First Docker build is **slow** (Maven + npm both build inside containers). Later builds are much faster thanks to layer caching.
- Backend image needs **JDK 20**; frontend image needs **Node 20**.
- The JRE runtime image has no `curl` preinstalled — if the `api` healthcheck fails for that reason, check `be-doc-doctor-ri/Dockerfile`.
- If Redis is down, booking will fail on purpose (slot hold depends on it) — this is expected, not a bug.
- For cloud deployment (Vercel + Railway) instead of local Docker, see [`deploy/README.md`](./deploy/README.md).
