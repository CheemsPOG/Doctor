# Doctor Ri Clinic – Frontend

Patient Portal cho mẹ bầu — UI sage/blush, Fraunces + Nunito.

## Chạy

```bash
npm install
npm test
npm run build
npm run dev   # http://127.0.0.1:5173  (proxy /api → :8080)
```

Demo login: `mebau@doctorri.local` / `Password123!`

## Cài như app điện thoại (PWA)

App hỗ trợ **Add to Home Screen** (iPhone / Android):

1. Build + phục vụ HTTPS (hoặc `localhost` khi dev):
   ```bash
   npm run build && npm run preview -- --host
   ```
2. Mở URL trên điện thoại (cùng Wi‑Fi: `http://<IP-máy>:<port>` — iOS khuyến nghị HTTPS cho production).
3. **iPhone (Safari):** Share → **Add to Home Screen**
4. **Android (Chrome):** menu ⋮ → **Install app** / **Add to Home screen**

Sau khi cài, icon **Doctor Ri** mở ở chế độ `standalone` (không thanh địa chỉ trình duyệt).

## Stack

- React 18, TypeScript, Vite
- React Router, TanStack Query, React Hook Form, Vitest

## Cấu trúc

```text
src/
├── app/                    # composition root
│   ├── providers/
│   ├── router/
│   └── styles/
├── pages/                  # route entry (auth / patient / clinic / doctor / admin)
├── features/               # bounded UI features
│   ├── auth/
│   ├── appointments/
│   ├── doctors/
│   ├── services/
│   ├── schedule/
│   ├── queue/
│   ├── encounter/
│   ├── obstetric/
│   ├── notifications/
│   ├── patients/
│   ├── clinic/
│   └── admin/
│       ├── domain/         # types, rules (no React)
│       ├── application/    # hooks / use cases
│       ├── infrastructure/ # API adapters
│       └── ui/             # feature components
├── entities/               # shared domain models
├── widgets/                # queue-board, appointment-calendar
└── shared/
    ├── api/                # httpClient
    ├── config/
    ├── hooks/
    ├── lib/
    ├── types/
    └── ui/
```

Chi tiết: [docs/adr/001-feature-clean-architecture.md](./docs/adr/001-feature-clean-architecture.md)

## Quy tắc phụ thuộc

```text
pages → features/ui → features/application → features/domain
                  ↘ features/infrastructure → shared/api → Backend /api/v1
```

- Không `fetch` trực tiếp trong `ui` / `pages`.
- Không import `features/A` từ `features/B` (dùng `entities` / `shared` nếu cần dùng chung).

## Chạy

```bash
npm install
npm run dev
```

Proxy Vite: `/api` → `http://localhost:8080`
