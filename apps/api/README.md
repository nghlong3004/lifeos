# LifeOS API

This is the repository's only implemented application scaffold. The
[POM](pom.xml) targets Java 25 and Spring Boot 4.1.1. Production code currently
provides the [Spring Boot entry point](src/main/java/me/nghlong3004/lifeos/LifeOSApplication.java);
the [test](src/test/java/me/nghlong3004/lifeos/LifeOSApplicationTests.java) only
attempts to load the application context.

No domain features, application endpoints, entities/repositories, migrations,
bot handlers, model tools, or custom security configuration are implemented.
Spring starters may contribute default behavior; that is not a verified
application authentication or integration contract.

## Build commands

Run from `apps/api/` with a JDK compatible with the Java 25 target and `JAVA_HOME`
or `java`/`javac` available. Maven is supplied by the wrapper; a separate system
Maven installation is not required.

```sh
sh ./mvnw --version
sh ./mvnw -DskipTests package
sh ./mvnw test
```

The Unix wrapper is tracked as mode `100644`; `sh` avoids relying on a local
executable-bit change. On Windows use `mvnw.cmd`. The
[wrapper properties](.mvn/wrapper/maven-wrapper.properties) select Maven 3.10.0.
First use downloads it, and builds resolve dependencies through Maven. There
is no root POM, Java formatter plugin, or checked-in CI workflow.

`-DskipTests package` compiles and packages without executing tests. It does not
verify the application context or external integrations.

## Runtime configuration

[application.yaml](src/main/resources/application.yaml) sets only
`spring.application.name`. There are no checked-in local/test profiles,
datasource credentials, provider keys, database containers, or migration files.
Supply runtime configuration through the environment or external Spring Boot
configuration; the application does not explicitly load a `.env` file.

| Environment variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL for an available PostgreSQL database |
| `SPRING_DATASOURCE_USERNAME` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `SPRING_AI_DEEPSEEK_API_KEY` | API key when DeepSeek chat auto-configuration is enabled |

DeepSeek property names come from the Spring AI 2.0.1 model auto-configuration
metadata. The model dependency does not provide a Telegram bot or application
tool execution. OAuth2 resource-server dependencies also do not supply an
issuer, JWT validation policy, or user ownership model.

Once an appropriate database and enabled provider configuration are supplied,
the launch command is:

```sh
sh ./mvnw spring-boot:run
```

Startup and authentication behavior must be observed in the configured
environment. There is no repository-provided complete local runtime recipe yet.
Keep keys, passwords, connection details, and personal data out of committed
files and logs. Do not call a paid model merely to verify documentation.

## Tests and formatting

The current `@SpringBootTest` has no test profile, mock provider, embedded
database, or wired Testcontainers fixture. Testcontainers dependencies are
declared, but do not provision PostgreSQL by themselves. Context loading needs
appropriate configuration for enabled auto-configurations; it is not a
feature-level test suite. Report failures and missing prerequisites separately.

Google Java Format and MapStruct are required project conventions. MapStruct,
Lombok, and their compiler processors are configured in the POM, but there are
no mapper interfaces yet. Google Java Format/Spotless enforcement is not
configured, so no `spotless:apply` or `spotless:check` command is currently
provided by this project. See [AGENTS.md](AGENTS.md) and the
[Java style skill](../../.agents/skills/lifeos-java-style/SKILL.md) before Java work.

The [API context skill](../../.agents/skills/lifeos-api-context/SKILL.md) owns
backend discovery and context maintenance. There is no `.agents/context/api/`
tree; use this README and source as the current entry points.
