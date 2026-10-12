# LifeOS — Repository Instructions

Scope: this repository. App-level instructions supplement this file.

## Workspace and evidence

Resolve the target checkout explicitly and inspect `git status` before work.
Read the shared contract at `~/.config/slp/workflow/WORKSPACE_PROTOCOL.md`, then
[docs/WORKSPACE_PROTOCOL.md](docs/WORKSPACE_PROTOCOL.md). If the shared contract
is unavailable, report the gap before dependent dispatch or acceptance.
Preserve unrelated edits, untracked work, and private installation/session state.

Use [README.md](README.md) for current repository status and
[docs/LifeOS_PRD.md](docs/LifeOS_PRD.md) for intended product behavior. Code,
configuration, and tests establish implementation; dependencies or PRD acceptance
criteria do not prove a feature exists. Currently only `apps/api/` is present;
web and infrastructure are planned.

## Context Management

Read repository-owned skills by path; do not depend on a globally installed
copy with the same name:

| Work scope | Skill |
| --- | --- |
| Repository or cross-app work | [.agents/skills/lifeos-context/SKILL.md](.agents/skills/lifeos-context/SKILL.md) |
| Backend discovery or changes | [.agents/skills/lifeos-api-context/SKILL.md](.agents/skills/lifeos-api-context/SKILL.md) |
| Creating, editing, or reviewing Java | [.agents/skills/lifeos-java-style/SKILL.md](.agents/skills/lifeos-java-style/SKILL.md) |
| Web work, once the application exists | [.agents/skills/lifeos-web-context/SKILL.md](.agents/skills/lifeos-web-context/SKILL.md) |

Before editing, consult the relevant skill and verify
existing context against current implementation.

After every coding task:

1. Review the final diff.
2. Evaluate whether documented knowledge changed.
3. Update only the affected context files.
4. Preserve unrelated changes.
5. Report whether context was updated.

For changes affecting multiple applications,
use the corresponding skills for each affected area.

Do not load every skill by default.
Do not update context for trivial changes that
leave documented behavior unchanged.

There is no `.agents/context/` tree yet. Prefer updating existing READMEs and
instructions when they already own the knowledge; create durable context only
when verified implementation warrants it. Keep skill names consistent with
their repository directories; do not rename them to match a private harness's
release conventions or copy private harness material into this repository.

## Verification and completion

Use the owning app's documented commands and prerequisites. For documentation
changes, check referenced paths, skill routing/frontmatter, and claims against
source. Java builds/tests run from `apps/api/` using `sh ./mvnw` on Unix; see the
[API README](apps/api/README.md). Report unavailable prerequisites and checks
not run; never turn a static inspection into a runtime success claim.

Review the final diff and update the existing source of project status when
accepted work changes it. A technical acceptance does not grant permission to
push, merge, deploy, message others, or spend money; use the authority supplied
for the current task.
