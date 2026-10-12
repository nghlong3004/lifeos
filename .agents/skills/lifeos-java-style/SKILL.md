---
name: lifeos-java-style
description: Apply mandatory Java 25, Spring Boot, Google Java Format, MapStruct, persistence, and testing conventions in LifeOS apps/api. USE WHEN creating, editing, or reviewing Java source. NOT FOR repository navigation or frontend work; use the relevant context skill.
---

# LifeOS — Java Coding Style

Scope: `apps/api/`. Read alongside root and `apps/api/AGENTS.md`.
Use existing verified conventions when they do not conflict with mandatory rules below.

## Policy and configured enforcement

The [POM](../../../apps/api/pom.xml) targets Java 25/Spring Boot 4.1.1 and
declares MapStruct 1.6.3, Lombok, `lombok-mapstruct-binding`, and their compiler
processors. It has no Google Java Format or Spotless plugin. There is no
checked-in CI formatter gate. Required coding policy below must not be reported
as automated enforcement. Current code is a scaffold; feature packages, JPA
entities, and mapper interfaces remain future work.

Read [API instructions](../../../apps/api/AGENTS.md) and the
[API README](../../../apps/api/README.md) for build/runtime limits.

## Workflow Routing

| Outcome | Guidance |
| --- | --- |
| Create, edit, or review Java | Apply the relevant conventions in sections 1–7 |
| Verify and hand off | Completion Review below and the repository-local API context skill |

## 1. Formatting — Google Java Format

- **`google-java-format` is the single source of truth for Java formatting.** Use its standard Google style (not AOSP).
- Follow its indentation, line wrapping, import ordering, and whitespace decisions; do not manually fight the formatter.
- Use explicit imports; avoid wildcard imports and fully qualified class names in method bodies.
- Prefer a Maven-integrated formatter (for example, Spotless configured to use `googleJavaFormat()`) so local development and CI share the same rules.
- **If Spotless is configured in future**, use the configured goals, typically `sh ./mvnw spotless:apply` for formatting and `sh ./mvnw spotless:check` for verification from `apps/api/`. Neither goal is currently configured. On Windows use `mvnw.cmd`.
- **If the formatter is not configured**, do not claim format checks passed. Flag the missing configuration rather than assuming these commands work.
- Avoid reformatting unrelated files as part of a focused task.

## 2. File Header — Mandatory

Every **new Java class, interface, enum, or record** must have type-level Javadoc, placed after imports and **before any type annotations**:

```java
/**
 * @author nghlong3004 (Long Nguyen Hoang)
 * @since 10/11/2026
 */
@Service
public class HabitService {
    // Example only; google-java-format controls actual formatting.
}
```

- Use `@since` equal to the **actual file creation date**, in `M/d/yyyy` format; `10/11/2026` is an example, not a permanent hardcoded date.
- Preserve the original `@since` when editing existing classes. Do not rewrite historical dates or fabricate a date if it is unknown.
- The Javadoc belongs to the type declaration, **not above the `package` line**.
- Do not add meaningless method comments or repeat names in Javadoc; document non-obvious behavior and important contracts.

## 3. Clean Code

- Optimize for readability, correctness, and maintainability—not minimum line count.
- Apply **SRP, DRY, KISS, YAGNI**, and separation of concerns pragmatically.
- Use descriptive names (`CreateExpenseRequest`, `TransactionService`); no cryptic abbreviations or magic values.
- Keep classes focused; split large classes/methods when they combine unrelated responsibilities. Do not apply arbitrary line-count limits.
- Prefer early returns over deep nesting; avoid boolean-flag parameters when they obscure intent.
- Prefer composition over inheritance; create interfaces and abstractions only when they provide a real boundary or variation.
- Reuse existing utilities when appropriate, but do not centralize unrelated domain logic into `common/`.
- Prefer straightforward loops or streams according to readability; use modern switch expressions where they make code clearer.
- Comments explain *why*, constraints, and tricky algorithms—not obvious code.

## 4. Java & Spring Conventions

- Target **Java 25**; use records for immutable request/response DTOs and value objects, not JPA entities.
- Organize packages **by feature** under `me.nghlong3004.lifeos`; put controllers, services, repositories, DTOs, and mappers in their owning feature.
- Keep controllers and external adapters thin; place application/domain decisions in services.
- Use constructor injection (`@RequiredArgsConstructor` for final dependencies, or explicit constructors); avoid field injection.
- Prefer `Optional` for appropriate return values, not entity fields or method parameters. Avoid returning `null` from public service contracts unless explicitly documented.
- Prefer immutable collections where suitable and avoid exposing mutable internal state.
- Use Lombok selectively (`@Getter`, `@Builder`, `@RequiredArgsConstructor`); avoid `@Data` on JPA entities.
- Prefer meaningful domain exceptions and structured error codes; do not throw generic `RuntimeException` for expected business failures.

## 5. Persistence & Domain Safety

- Prefer `UUID` IDs for new domain entities unless a documented integration or database reason requires otherwise.
- Use `Instant` or `OffsetDateTime` for timestamps, `LocalDate` for business calendar dates, and injected `Clock` when time control matters for tests.
- Use **`BigDecimal`** for money; define rounding and currency assumptions explicitly.
- Place transaction boundaries at service operations when needed (`@Transactional`); prevent double-processing and preserve history.
- Use Flyway migrations for schema changes; check constraints, ownership, and backward compatibility.
- Do not rely on JPA entities as public response schemas; avoid N+1 queries and accidental lazy loading in API serialization.

## 6. MapStruct — Default and Required

- Use MapStruct for nontrivial object mapping instead of manual per-field assignment or reflection mappers.
- Mapper interfaces belong in `<feature>/mapper/`; use `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)` or a shared `@MapperConfig` that enforces the same policy.
- Make intentional ignores and special mappings explicit, especially for ID, owner, role, monetary, and audit fields.
- Restrict request-to-entity mappings to allowlisted user-editable fields (`@BeanMapping(ignoreByDefault = true)` is an option).
- Never put domain decisions, authorization, network calls, or repository queries in mappers.
- Hand-written mapping is acceptable for trivial scalar/value conversion; any larger exception must be justified.
- Do not edit generated `*MapperImpl` classes. Verify generated mappings at compile time.

## 7. Security, Logging & Testing

- Validate request payloads with Jakarta Validation and enforce authorization in backend services/boundaries.
- Do not trust LLM/tool output; domain services validate and persist actions only after required confirmation.
- Use `@Slf4j` when logging is needed; record meaningful state transitions and unexpected failures, never secrets, tokens, or sensitive PII/finance payloads.
- Write focused unit tests for business rules and integration tests for persistence, authorization, retry, and idempotency where relevant.
- Avoid test code that depends on current wall-clock time or mutable shared state.

## 8. Completion Review

1. Check the diff for Clean Code, domain boundaries, and any manual mapping that should use MapStruct.
2. Ensure new Java types have the correct Javadoc author/since header.
3. Run configured format checks and focused tests, then `sh ./mvnw test` from `apps/api/` when feasible. The wrapper is tracked without an executable bit; use `mvnw.cmd` on Windows. The context-loading test needs configuration for enabled database/provider auto-configurations; dependencies alone do not provision test fixtures.
4. Report checks that could not run; do not claim formatting is enforced until formatter tooling is configured.
5. Follow `lifeos-api-context` to synchronize **only** changed, verified architectural knowledge.

## Gotchas

- Declared MapStruct processors are not evidence that mappings compile until the relevant build runs; generated implementations are never hand-edited.
- Preserve existing creation dates and unrelated formatting. A policy review does not authorize bulk changes to unrelated Java files.

## Examples

- Add a request/response mapper: keep it in the owning feature, allowlist untrusted fields, enforce unmapped-target errors, and compile the generated mapping.
- Review a Java change: inspect relevant domain boundaries and headers, run available checks, and distinguish missing formatter enforcement from a formatting pass.
