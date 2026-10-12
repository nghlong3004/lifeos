---
name: lifeos-web-context
description: Discover and maintain verified LifeOS web guidance once apps/web exists. USE WHEN implementing or reviewing web routes, components, state, API access, styling, or Telegram Mini App behavior. NOT FOR backend-only tasks or scaffolding a frontend without user authorization.
---

# LifeOS — Web Context

Scope: `apps/web/`, when it exists. Follow root and any `apps/web/AGENTS.md` first.

## Availability

There is currently no `apps/web/`, frontend `package.json`, web instruction
file, or `.agents/context/web/` tree. The [root README](../../../README.md)
records this status; [the PRD](../../../docs/LifeOS_PRD.md) describes the planned
dashboard/Mini App. Recheck availability before using the workflow below.

If the app is absent, report the missing implementation and continue any
independent authorized work. Do not invent routes, libraries, npm scripts,
Telegram SDK wiring, or create frontend scaffolding solely to satisfy this
skill. Building the app requires scope that authorizes that work.

## Workflow Routing

| Outcome | Guidance |
| --- | --- |
| Locate and change an existing web feature | Find the implementation and Change safely below, once the app exists |
| Maintain verified web knowledge | Sync Web context after editing below |

## Find the implementation

1. Inspect relevant changes, `package.json`, and the actual project tree.
2. Read `.agents/context/web/README.md` and relevant feature context **only if they exist**.
3. Identify the route/screen and owning feature; follow component -> state/hook -> API client -> backend contract as relevant.
4. Discover the router, state and styling libraries from actual configuration. Do not assume specific frontend libraries are installed.
5. For Mini App flows, check Telegram SDK initialization in the web app and server-side validation of Telegram `initData` in the API.

| Concern | Starting point |
| --- | --- |
| Page/route | Configured router and owning screen |
| Feature behavior | Feature components, hooks, services, types |
| Data fetching | Shared API client, queries and API contracts |
| Shared UI | Existing component library and styles |
| Telegram Mini App | Telegram integration and app entry point |
| Build/tests | `package.json`, configured tooling and tests |

## Change safely

- Preserve authenticated API behavior and error/loading states.
- Check responsive and Telegram WebView behavior when relevant.
- Verify contract changes with the API; never trust frontend-supplied user identity without backend validation.
- Reuse established components/patterns where appropriate.
- Use commands actually declared by the app. Verify visible UI and interactions in a browser when claiming they work; a build or API response alone does not prove usability.

## Sync Web context after editing

1. Review the final diff for changed feature ownership, routes, data flows, API integration or UI architecture.
2. If existing context is affected, replace outdated facts in the relevant file.
3. If context does not exist, **do not create placeholders**. Create only when actual, verified implementation deserves persistent guidance:
   - `.agents/context/web/README.md`: short feature/route index.
   - `.agents/context/web/features/<feature>.md`: ownership, key flows, contracts, reusable patterns, tests and constraints.
4. Update parent indexes only when feature navigation/ownership changes. Do not duplicate implementation or maintain a changelog here.

Cosmetic changes normally do not need context edits. Report updated, unchanged or not initialized.

## Gotchas

- A planned React/Mini App requirement does not establish installed packages or an accepted backend authentication contract.
- Read the repository's [API context skill](../lifeos-api-context/SKILL.md) when tracing server validation; client `initData` handling is only one side of the boundary.

## Examples

- Review current web setup: confirm that the app is absent and correct status/navigation guidance without inventing build commands.
- Change a future Mini App screen: discover its actual router, SDK, API contract, and scripts; verify relevant browser behavior and server validation.
