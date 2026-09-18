# ADR 001: Feature Clean Architecture (FSD-inspired)

**Status:** Accepted  
**Date:** 2026-07-20

## Context

- Spec: React + TypeScript, Patient Portal + Clinic Portal.
- Cần tách UI khỏi API và domain để tái sử dụng giữa portal bệnh nhân / lễ tân / bác sĩ.

## Decision

Áp dụng Clean Architecture theo **feature**, kết hợp FSD nhẹ:

| Layer | Path | Trách nhiệm |
|---|---|---|
| `domain` | `features/*/domain` | Types, rules thuần (không React) |
| `application` | `features/*/application` | Hooks / use-case orchestration (TanStack Query) |
| `infrastructure` | `features/*/infrastructure` | API client, mappers DTO ↔ domain |
| `ui` | `features/*/ui` | Components của feature |
| `pages` | `pages/*` | Composition route |
| `shared` | `shared/*` | httpClient, config, UI primitives |
| `entities` | `entities/*` | Model dùng chung nhiều feature |
| `widgets` | `widgets/*` | Khối UI lớn (queue board, calendar) |

Quy tắc phụ thuộc: `ui → application → domain`; `infrastructure` implement gọi API; `pages` compose features.

## Consequences

- Positive: feature độc lập; dễ map với BE modules; test domain/hooks tách UI.
- Negative: cần alias path và kỷ luật import (không gọi `fetch` trực tiếp trong UI).

## Alternatives considered

- Flat `components/` + `api/` theo spec 10.3 — đủ cho prototype, khó scale theo portal.
- Full FSD cứng — quá nhiều tầng cho MVP; giữ subset.
