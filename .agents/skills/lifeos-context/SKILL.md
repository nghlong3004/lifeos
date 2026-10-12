---
name: lifeos-context
description: Navigate the LifeOS repository, locate application owners, and maintain verified cross-app guidance. USE WHEN exploring the repository, reviewing project documentation, changing infrastructure, or tracing cross-app contracts. NOT FOR Java conventions or app internals; use the matching API, Java style, or web skill.
---

# LifeOS — Repository Context

Scope: repository-level navigation and cross-application relationships, **not** internal Java or React implementation details.

## Current repository entry points

Read [root instructions](../../../AGENTS.md), the
[workspace protocol](../../../docs/WORKSPACE_PROTOCOL.md), and
[root README](../../../README.md). The only current app is the Spring Boot
backend at `apps/api/` (containing the application scaffold and prepared
telegram-echo integration); `apps/web/`, `infra/`, Compose files,
`docs/architecture/`, and `.agents/context/` are absent. The README owns current
status; `docs/LifeOS_PRD.md` owns intended behavior. Recheck the tree before
treating a planned component as available.

## Workflow Routing

| Outcome | Guidance |
| --- | --- |
| Locate current owners and contracts | Locate the relevant code below; use repository-local app skills |
| Maintain durable project knowledge | Future living context and After changes below |

## Locate the relevant code

1. Read root `AGENTS.md` and inspect `git status`.
2. Identify the task's owner and validate paths from the repository tree.
3. Read the relevant app's `AGENTS.md` (if present), then its repository-local context skill:
   - Backend: [lifeos-api-context](../lifeos-api-context/SKILL.md), plus [lifeos-java-style](../lifeos-java-style/SKILL.md) for Java creation/edit/review.
   - Web, once present: [lifeos-web-context](../lifeos-web-context/SKILL.md).
4. For changes crossing app boundaries, trace and verify both sides of the API/data contract.
5. Read relevant architecture docs for intent; implementation and tests describe current behavior.

## Navigation hints

| Area | Likely path |
| --- | --- |
| Backend application | `apps/api/` |
| Development scripts | `scripts/` |
| Web application (when present) | `apps/web/` |
| Deployment/infrastructure (when present) | `infra/` and root Compose files |
| Product/architecture documents | `docs/` |
| Agent rules | Root and app-level `AGENTS.md` |
| Living context (when created) | `.agents/context/` |

These paths are hints, not assertions that files already exist.

## Future living context

When there is **verified, durable, implemented knowledge** worth recording, store repository-wide facts in `.agents/context/README.md`:

- Implemented applications and responsibilities.
- Contracts and flows that connect applications.
- Shared configuration or infrastructure constraints.
- Links to existing `.agents/context/api/README.md` or `web/README.md`.

Do **not** create empty context or placeholder indexes during initial setup. Don't copy feature internals into the root context file.
Prefer existing README/AGENTS guidance when it already owns the facts; create
separate living context only when it adds useful implementation knowledge.

## After changes

Review the diff. Update repository-level context **only** when repository organization, app responsibilities, cross-app contracts or shared infrastructure change. Delegate app details to their respective skills/context files. Replace obsolete statements rather than appending changelog entries.

Finish with a concise context status; distinguish a missing/uninitialized context from an unchanged existing context.

## Gotchas

- This product repository and a personal LifeOS harness installation are separate systems. Global skill copies, private configuration, hooks, and session state are not repository inputs to publish.
- A dependency, PRD requirement, or proposed directory is not implemented behavior. Report missing owners or runtime prerequisites without inventing them.

## Examples

- Review setup guidance: compare the READMEs with the POM, wrapper, source, and tree; update the document that owns each stale claim.
- Trace a future Mini App contract: verify both actual web and backend owners before describing authentication or data flow; if web is absent, report that dependency.
