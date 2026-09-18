# ADR 001: Clean Architecture + Modular Monolith

**Status:** Accepted  
**Date:** 2026-07-20

## Context

- Spec đề xuất modular monolith (`auth`, `appointment`, `queue`, …) cho MVP.
- Stack: Java 21, Spring Boot, JPA, Flyway, MySQL (`db_clinic`), Redis.
- Cần tách rõ domain khỏi framework để test và mở rộng (ví dụ tách Notification sau này).

## Decision

Dùng **một deployable** (modular monolith) với **Clean Architecture theo từng bounded context**:

| Layer | Trách nhiệm | Phụ thuộc |
|---|---|---|
| `domain` | Entity, value object, domain service, repository port | Không phụ thuộc Spring/JPA |
| `application` | Use case, inbound/outbound port, DTO | Chỉ phụ thuộc `domain` |
| `infrastructure` | JPA, Redis, MinIO, security, outbox | Implement port |
| `presentation` | REST controller, mapper, advice | Gọi use case |

Package root: `com.doctorri.clinic.<module>.<layer>`

## Consequences

- Positive: ranh giới module rõ; dễ tách microservice sau; unit test domain/use case không cần DB.
- Negative: nhiều package hơn layered classic; cần discipline không gọi JPA từ controller.

## Alternatives considered

- Pure layered (`controller` / `service` / `repository`) — nhanh nhưng khó tách module.
- Multi-module Maven ngay từ đầu — overhead cho MVP một team.
