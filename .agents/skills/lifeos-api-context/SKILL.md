---
name: lifeos-api-context
description: Discover and maintain verified guidance for the LifeOS backend in apps/api. USE WHEN tracing backend code, configuration, tests, or implementing REST, persistence, security, AI, Telegram, scheduling, or export features. NOT FOR Java style alone or web internals; use their repository skills. Feature names identify task scope, not implementation status.
---

# LifeOS — API Context

Scope: `apps/api/`. Follow root and `apps/api/AGENTS.md` first.

## Current implementation and commands

The [API README](../../../apps/api/README.md),
[API instructions](../../../apps/api/AGENTS.md),
[POM](../../../apps/api/pom.xml), and
[configuration](../../../apps/api/src/main/resources/application.yaml) are the
current entry points. The app contains a Spring Boot entry point and a
context-loading test. Domain features, custom security, migrations, handlers,
and tools are not implemented; `.agents/context/api/` does not exist.

Run Maven from `apps/api/` with a JDK compatible with Java 25:
`sh ./mvnw -DskipTests package` for packaging and `sh ./mvnw test` for tests
(`mvnw.cmd` on Windows). The Unix wrapper is not tracked as executable.
Packaging without test execution is not a startup or test pass. Consult the
README for database/provider prerequisites; no test fixture provisions them.

Use [lifeos-java-style](../lifeos-java-style/SKILL.md) for Java creation,
editing, or review. MapStruct processors are declared, but formatter tooling
and mapper interfaces are not currently configured/implemented.

## Workflow Routing

| Outcome | Guidance |
| --- | --- |
| Locate and verify backend behavior | Find the implementation and Change safely below |
| Maintain affected documentation | Sync API context after editing below |

## Find the implementation

1. Inspect relevant changes and `apps/api/pom.xml` as needed.
2. Read `.agents/context/api/README.md` and the relevant feature context **only if they exist**.
3. Find the owning package under `apps/api/src/main/java/`; trace entry point -> service -> repository/integration and impacted callers.
4. Verify behavior against source, migrations, configuration and relevant tests; PRD describes planned behavior, not implementation status.

| Concern | Starting point |
| --- | --- |
| REST endpoint | Controller, request/response DTO, service |
| Business rule | Owning service/domain model and tests |
| Persistence | Entity, repository, `src/main/resources/db/migration/` |
| Mapping | MapStruct mapper and DTOs |
| AI & tool calls | Provider configuration, tool handlers, validation, confirmation |
| Telegram & reminders | Bot handlers, scheduling, notification persistence |
| Security | Authentication, authorization, ownership checks |
| Configuration | `pom.xml`, `application*.yaml` (also check for `.yml`/`.properties` if added) |
| Tests | `src/test/` |

Verify actual module names/locations before using them.

## Change safely

- Inspect both producers and consumers when shared contracts change.
- For finance, verify exact arithmetic, integrity and idempotency.
- For AI, verify tool schema, authorization, confirmation and backend-owned execution.
- For scheduled/Telegram operations, verify retry and deduplication behavior.
- Add focused tests and migrations where required.

## Sync API context after editing

1. Review the final diff and determine whether documented knowledge changed.
2. If relevant context exists, update only affected statements; remove stale paths and facts.
3. If context does not exist, **do not create it by default**. Only when implemented, verified knowledge would meaningfully help future work, create:
   - `.agents/context/api/README.md` for a compact module/feature index.
   - `.agents/context/api/features/<feature>.md` for substantial feature-specific knowledge.
4. A feature file should capture **current** purpose, code ownership, main flows, critical rules, contracts/data, tests and known constraints. Omit irrelevant sections.
5. Keep index links valid; avoid duplicating PRD details or recording task-by-task history.

Update existing README/instructions when they already own the changed facts;
do not create a second scaffold summary in `.agents/context/api/`.

Do not force updates for cosmetic edits or bug fixes that leave existing context accurate. Report whether context was updated, unchanged or not initialized.

## Gotchas

- `application.yaml` sets only the application name. Declared PostgreSQL, AI, security, and Testcontainers dependencies do not supply credentials, authorization policy, or fixtures.
- Do not infer runtime success or an endpoint contract from starter dependencies. Verify effects in an appropriately configured environment and report untested integrations.

## Examples

- Review build instructions: resolve versions and annotation processors from the POM, and use wrapper commands with their actual prerequisites.
- Add a future financial operation: locate or implement its owning service, validate money/ownership/retries, and document only behavior demonstrated by relevant tests.
