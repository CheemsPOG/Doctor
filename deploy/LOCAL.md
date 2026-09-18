# Doctor Ri — chạy full stack trên Mac bằng Docker

## Yêu cầu
- Docker Desktop (Mac) đang chạy
- Port trống: `8088` (FE), `8080` (API) — đổi trong `.env` nếu cần

## Lên nhanh

```bash
cd /Users/hieu.bui/Documents/doctor/deploy
cp -n .env.example .env   # lần đầu
docker compose up -d --build
```

Mở: **http://localhost:8088**

| Service | URL trong Docker | Port host mặc định |
|---------|------------------|--------------------|
| FE (nginx) | `web:80` | **8088** |
| BE API | `api:8080` | 8080 |
| MySQL | `mysql:3306` | 3307 |
| Redis | `redis:6379` | 6380 |

FE gọi API qua **cùng origin** (`/api/v1` → nginx proxy → `api:8080`), nên điện thoại cùng Wi‑Fi chỉ cần:

`http://<IP-Mac>:8088`

## Tài khoản demo
- `mebau@doctorri.local` / `Password123!`
- Staff: `receptionist@…`, `doctor.ri@…`, `admin@…` (cùng password)

## Lệnh hữu ích

```bash
# Log BE
docker compose logs -f api

# Health
curl -s http://localhost:8080/actuator/health

# Tắt
docker compose down

# Xoá cả data MySQL/Redis
docker compose down -v
```

## Lưu ý
- Lần build đầu **lâu** (Maven + npm).
- Image BE cần JDK 20; FE Node 20.
- JRE image không có `curl` sẵn — nếu healthcheck api fail, cài curl trong Dockerfile BE hoặc đổi healthcheck. (xem bên dưới nếu cần)

## Cloud (Vercel / Railway)
Xem `README.md` cùng folder `deploy/`.
