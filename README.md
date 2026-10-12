# LifeOS

LifeOS is a personal life assistant under development. The intended product
uses Telegram and AI to help manage habits, tasks, finances, goals, and reports.
The [product requirements](docs/LifeOS_PRD.md) describe that planned behavior;
they are not an inventory of implemented features.

## Current implementation

The repository contains a Spring Boot backend scaffold in `apps/api/`: an
application entry point, dependency/configuration files, and a context-loading
test. There are no application REST controllers, domain services, entities,
repositories, Flyway migrations, Telegram handlers, or AI tools yet. Dependencies
alone do not implement those capabilities.

The React dashboard/Telegram Mini App and Docker infrastructure are planned.
There is currently no `apps/web/`, `infra/`, Compose file, or frontend package
manifest. Habits, finance, goals, reminders, analytics, and Excel export remain
product requirements.

## Declared backend stack

The [POM](apps/api/pom.xml) and
[wrapper configuration](apps/api/.mvn/wrapper/maven-wrapper.properties) own
the versions:

| Component | Declared configuration |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.1.1 |
| Maven Wrapper | Maven 3.10.0, wrapper 3.3.4 |
| Spring AI | 2.0.1 BOM, DeepSeek model starter |
| Persistence | Spring Data JPA, PostgreSQL driver, Flyway |
| HTTP/security | Web MVC, Validation, Security, OAuth2 resource server, Actuator, springdoc |
| Telegram | Long-polling and client dependencies 10.3.0 |
| Mapping | MapStruct 1.6.3, Lombok, annotation processors |

The PRD's proposed Java 21/OpenAI stack is design context. Use the current POM
for build and provider configuration. Authentication policy, integrations,
database provisioning, and deployment have not been implemented here.

## Working locally

Install a JDK compatible with the Java 25 target. Run Maven commands from
`apps/api/`; there is no root Maven project. The Unix wrapper is tracked without
an executable bit, so invoke it with `sh`:

```sh
cd apps/api
sh ./mvnw --version
sh ./mvnw -DskipTests package
```

The wrapper downloads Maven on first use; dependency resolution also requires
access to Maven repositories or a populated local cache. On Windows use
`mvnw.cmd` with the same goals.

See the [API README](apps/api/README.md) for runtime configuration, tests, and
known limitations. Packaging with skipped tests does not prove application
startup. A datasource and enabled AI provider configuration are not supplied by
the checked-in YAML, so this is not a ready-to-run product.

## Repository guidance

```text
lifeos/
├── .agents/skills/       # Repository-owned context and Java style skills
├── apps/api/            # Backend scaffold, Maven Wrapper, API guidance
├── docs/
│   ├── LifeOS_PRD.md     # Intended product behavior
│   └── WORKSPACE_PROTOCOL.md
├── AGENTS.md            # Repository agent instructions
└── README.md
```

Start with [AGENTS.md](AGENTS.md) and the
[workspace contract](docs/WORKSPACE_PROTOCOL.md). The skill files under
`.agents/skills/` are project instructions; they do not install or activate a
personal LifeOS harness, hooks, dashboards, or services. No CI workflow or
Java formatter task is checked in. See
[API instructions](apps/api/AGENTS.md) for required coding conventions and the
difference between policy and configured enforcement.

## License

No license has been selected or checked in.
