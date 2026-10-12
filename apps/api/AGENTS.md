# LifeOS API — Agent Instructions

Scope: `apps/api/`. These instructions supplement the repository-root `AGENTS.md`.

## Current Implementation

Read [README.md](README.md) for commands, configuration, and limits. This app is
a Spring Boot scaffold, not an implemented habit/finance/Telegram service.
`pom.xml` declares integrations; source and tests determine which are wired.
There are no migrations, feature packages, custom security configuration, or
configured formatter task yet. The rules below govern new work; they do not
claim those components already exist.

## Before Editing

1. Read the root `AGENTS.md` and inspect `git status`; preserve unrelated changes.
2. Follow these repository-root skills for backend tasks:
   - `.agents/skills/lifeos-api-context/SKILL.md` — locate owners and synchronize verified context.
   - `.agents/skills/lifeos-java-style/SKILL.md` — **mandatory** when creating, editing, or reviewing Java code.
3. Inspect relevant source, configuration, migrations, and tests. Trace entry point → service → persistence/integration → affected consumers.
4. Treat PRDs as intended behavior, not proof that a feature is implemented.

## Mandatory Java Rules

- Target Java 25 and Spring Boot 4.x; build with Maven.
- **Use `google-java-format` as the canonical Java formatter.** Do not substitute a different style or manually override formatter output. Use the repository's formatter task if configured; none is configured currently. Do not add formatter tooling as an incidental documentation change.
- **Clean Code is required:** descriptive names, focused classes/methods, low coupling, no duplicated business logic, no speculative abstractions, and no overengineering.
- **Every newly created Java class, interface, enum, or record must have type-level Javadoc** with `@author nghlong3004 (Long Nguyen Hoang)` and `@since` set to its creation date in `M/d/yyyy` format (example: `10/11/2026`). Place Javadoc immediately before type annotations/declaration; preserve existing `@since` when modifying a file.
- Organize code by feature; keep REST controllers, Telegram handlers, schedulers, and AI tools as thin adapters. Put business decisions in the owning application/domain service.
- Prefer constructor injection, immutable API payloads (`record`), and explicit imports. Do not expose JPA entities as API contracts.

## Object Mapping — Mandatory

- **Use MapStruct by default** for Entity ↔ DTO, Entity → Response, Request → DTO/entity, and other nontrivial object-to-object mappings.
- Place mapper interfaces in the owning feature's `mapper` package; use Spring component model and `ReportingPolicy.ERROR` (prefer a shared `@MapperConfig` if present).
- Declare explicit `@Mapping`, `ignore`, or `@BeanMapping` rules where needed; never silently map untrusted input into IDs, ownership, balances, audit fields, or authorization state.
- **Do not** replace MapStruct with manual field-by-field mapping, `BeanUtils.copyProperties`, ModelMapper, or reflection-based mapping. Trivial scalar conversions or single-value construction are exceptions; justify other exceptions.
- Keep business rules and repository access out of mappers. Never handwrite MapStruct-generated implementations.

## Backend Safety

- Validate inputs and enforce authorization on user-owned data at backend boundaries.
- AI output must not bypass domain validation, permissions, or confirmation. Never claim a write succeeded before it is confirmed.
- Use `BigDecimal` for money with explicit rounding rules; preserve transaction consistency and financial history.
- Make important writes and Telegram retries idempotent; guard scheduler and notification duplication.
- Use Flyway for schema changes. Keep secrets and sensitive personal/financial data out of logs.
- Add or update focused tests for business behavior, boundaries, failures, authorization, and retries as appropriate.

## Before Finishing

1. Review the final diff for correctness, Clean Code, and required MapStruct usage.
2. Apply/check Google Java Format **if formatter tooling exists**; otherwise report that enforcement is not configured rather than claiming formatting passed.
3. Run relevant tests, then `sh ./mvnw test` from `apps/api/` when feasible (`mvnw.cmd test` on Windows). The wrapper is not tracked as executable. The current context-loading test has no database/provider fixture; consult the README for prerequisites and report any checks not run. Skipping tests while packaging is not a test pass.
4. Follow `lifeos-api-context` to review `.agents/context/api/` freshness. Create context only when verified implementation warrants it; never create placeholder trees or duplicate the PRD.
5. Report changes, verification results, and `Context: updated / unchanged / not initialized`.
