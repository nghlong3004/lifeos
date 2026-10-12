# LifeOS Workspace Protocol

This document supplements the shared contract at
`~/.config/slp/workflow/WORKSPACE_PROTOCOL.md`; it does not change role authority
or safety boundaries. Read both against the explicitly resolved checkout.

## Repository Understanding

Repository code, types, module interfaces, executable commands, tests, examples,
and runtime evidence hold current implementation knowledge. Product intent,
rationale, and accepted constraints remain with their repository owners.
Use the [root README](../README.md), [repository guide](../AGENTS.md), and the
available app-specific guides to find relevant sources. Currently `apps/api/`
is the only application; its [README](../apps/api/README.md) and
[instructions](../apps/api/AGENTS.md) describe the scaffold and its limits.
[LifeOS_PRD.md](LifeOS_PRD.md) holds intended product behavior. There is no
`apps/web/`, `infra/`, or `docs/architecture/` tree yet. Link to existing
knowledge rather than reproduce it in a new summary.

## Authority And Scope

Respect the user's requested scope. Discovery does not authorize editing,
starting services, installing tools, or changing external settings.
Before changing externally observable policy, identify accepted repository
authority. If materially different choices remain, request the smallest decision
and pause the affected action while independent authorized work continues.
Code and tests show current behavior; configurable defaults are not
authority for missing product policy.

## Missing Capabilities

Identify the capabilities needed for the requested outcome: locating an owner
or interface, running a command, preparing known state, observing an effect,
verifying behavior, or recovering safely. Check the relevant implementation,
commands, fixtures, logs, and environment rather than assuming they exist.
Report an unavailable capability with its evidence and owning repository or
external dependency. Keep unobserved state Unknown.

Application commands, prerequisites, readiness, state ownership, and cleanup
belong to this repository. Preserve unrelated work and private installation or
session state. A template or dependency supplies structure, not proof that an
application can be operated. Do not invent missing commands or policy.

## Trust Through Evidence

Use repository-native checks and observations appropriate to the claimed
behavior. Protect accepted safety and correctness boundaries; preserve existing
state and verified recovery mechanisms. Distinguish observed results from
assumptions, and structural checks from semantic or runtime proof.

A plan, checklist, or completion message is not behavior-level proof. A checked-in
CI job does not prove it ran or blocks merging. Report what was exercised, what
failed or could not run, and what remains unverified.

## Project Skills and Handoff

Use the repository-owned skills linked in [AGENTS.md](../AGENTS.md) for the
relevant scope. They are project guidance, not an installation of a personal
harness. Do not publish private instructions, notebooks, runtime state, or
conversation transcripts in project documents.

Use the existing README status and affected instructions/context to record
accepted changes and remaining limits. No separate project task tracker or
living context tree is currently checked in; do not create a duplicate tracker
or placeholder context. Technical acceptance needs evidence and does not grant
external-effect authority. Follow permission already supplied for the task.
