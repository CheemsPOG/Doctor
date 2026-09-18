# Deploy Doctor Ri

| Layer | Target | Config |
|-------|--------|--------|
| **Full stack trên Mac** | **Docker Compose** | `deploy/docker-compose.yml` → xem `LOCAL.md` |
| FE React (Vite/PWA) | **Vercel** | `deploy/vercel/`, `fe-doc-doctor-ri/vercel.json` |
| BE Spring Boot | **Railway** | `deploy/railway/`, `be-doc-doctor-ri/Dockerfile` |
| MySQL | **Railway** | plugin + env map |
| Redis | **Railway** | plugin — **required** (slot hold / idempotency) |

## Local Mac (Docker) — nhanh

```bash
cd deploy
cp -n .env.example .env
docker compose up -d --build
# → http://localhost:8088
```

Chi tiết: [`LOCAL.md`](./LOCAL.md)

## Quick start order

1. Railway project → add **MySQL** + **Redis**
2. Railway service → **BE** (`be-doc-doctor-ri`, Dockerfile)
3. Set BE env from `deploy/railway/.env.be.example` (reference MySQL/Redis vars)
4. Smoke: `GET https://<be>/actuator/health`
5. Vercel → import `fe-doc-doctor-ri`, set `VITE_API_BASE_URL`
6. Set BE `CORS_ALLOWED_ORIGINS` to Vercel URLs (include `https://*.vercel.app` for previews)
7. Login smoke on FE production URL

## FE (Vercel)

- Root Directory: `fe-doc-doctor-ri`
- Framework: Vite
- Build: `npm run build`
- Output: `dist`
- Env: `VITE_API_BASE_URL=https://<be-host>/api/v1`
- SPA: `vercel.json` already in FE root

Template: `deploy/vercel/.env.example`

## BE (Railway)

- Root Directory: `be-doc-doctor-ri`
- Builder: Dockerfile (`be-doc-doctor-ri/Dockerfile`)
- Healthcheck: `/actuator/health`
- JDK: 20
- Bind port: Railway `PORT` (mapped in `application.yml` as `${PORT:...}`)

Env template: `deploy/railway/.env.be.example`  
Var mapping: `deploy/railway/.env.mysql-redis.example`

### BE env highlights

| Env | Required |
|-----|----------|
| `MYSQL_*` | Yes |
| `REDIS_*` | Yes |
| `JWT_SECRET` | Yes (strong, not local default) |
| `CORS_ALLOWED_ORIGINS` | Yes (Vercel origins) |
| `PUSH_PROVIDER` | Optional (`STUB` OK) |

## Checklist smoke

- [ ] `/actuator/health` → UP  
- [ ] FE login demo account  
- [ ] No CORS errors  
- [ ] Book appointment (uses Redis)  
- [ ] `/patient/results` OK (Flyway V5)  
- [ ] PWA installable (HTTPS)

## Code changes for prod readiness

- `CORS_ALLOWED_ORIGINS` env (+ wildcard patterns for `*.vercel.app`)
- `server.port=${PORT:...}` for Railway
- FE `vercel.json` SPA rewrites
- BE `Dockerfile` (Temurin 20)

Chi tiết checklist cũ: giữ trong git history nếu cần; file này là nguồn vận hành chính.
