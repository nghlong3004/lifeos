# LifeOS API

This is the repository's only implemented application scaffold. The
[POM](pom.xml) targets Java 25 and Spring Boot 4.1.1. Production code currently
provides the [Spring Boot entry point](src/main/java/me/nghlong3004/lifeos/LifeOSApplication.java)
and a minimal plain-text Telegram echo integration in
[me.nghlong3004.lifeos.telegram](src/main/java/me/nghlong3004/lifeos/telegram/);
the [scaffold test](src/test/java/me/nghlong3004/lifeos/LifeOSApplicationTests.java) only
attempts to load the application context.

No domain features, application endpoints, entities/repositories, migrations,
model tools, or custom security configuration are implemented.
Spring starters may contribute default behavior; that is not a verified
application authentication or integration contract.

## Build commands

Run from `apps/api/` with a JDK compatible with the Java 25 target and `JAVA_HOME`
or `java`/`javac` available. Maven is supplied by the wrapper; a separate system
Maven installation is not required.

```sh
sh ./mvnw --version
sh ./mvnw -DskipTests package
sh ./mvnw test -Dtest=Telegram*Test,Jackson2ConfigurationTest
sh ./mvnw test
```

The Unix wrapper is tracked as mode `100644`; `sh` avoids relying on a local
executable-bit change. On Windows use `mvnw.cmd`. The
[wrapper properties](.mvn/wrapper/maven-wrapper.properties) select Maven 3.10.0.
First use downloads it, and builds resolve dependencies through Maven. There
is no root POM, Java formatter plugin, or checked-in CI workflow.

`test -Dtest=Telegram*Test,Jackson2ConfigurationTest` runs only the prepared isolated unit tests for the
Telegram echo package and shared Jackson 2 configuration, excluding the unconfigured `@SpringBootTest` context test.
`-DskipTests package` compiles and packages without executing tests. It does not
verify the application context or external integrations. No Java runtime was
available in the inspection environment, so tests have been prepared and statically
inspected against dependency metadata rather than executed at runtime.

## Runtime configuration

[application.yaml](src/main/resources/application.yaml) sets `spring.application.name`
and binds `lifeos.telegram.bot-token` from `TELEGRAM_BOT_TOKEN` with an empty default.
A dedicated development profile `telegram-echo` is configured in
[application-telegram-echo.yaml](src/main/resources/application-telegram-echo.yaml)
to run the minimal plain-text echo route without requiring a database, JPA, Flyway,
or AI model credentials. There are no checked-in datasource credentials, provider keys,
database containers, or migration files. Supply runtime configuration through the
environment or external Spring Boot configuration; the application does not explicitly
load a `.env` file.

| Environment variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL for an available PostgreSQL database |
| `SPRING_DATASOURCE_USERNAME` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `SPRING_AI_DEEPSEEK_API_KEY` | API key when DeepSeek chat auto-configuration is enabled |
| `TELEGRAM_BOT_TOKEN` | Bot token for the Telegram plain-text echo integration (`telegram-echo` profile) |

DeepSeek property names come from the Spring AI 2.0.1 model auto-configuration
metadata. The model dependency does not provide a Telegram bot or application
tool execution. OAuth2 resource-server dependencies also do not supply an
issuer, JWT validation policy, or user ownership model.

### Telegram echo integration (`telegram-echo` profile)

The `me.nghlong3004.lifeos.telegram` package provides a minimal, plain-text echo route
using the TelegramBots 10.3.0 library:
- Configuration: `lifeos.telegram.bot-token` bound from `TELEGRAM_BOT_TOKEN`.
  `TelegramProperties.toString()` masks the token to prevent credential exposure.
- Shared Jackson 2 mapper: `Jackson2Configuration` in `me.nghlong3004.lifeos.config`
  provides an explicitly owned `@ConditionalOnMissingBean(ObjectMapper.class)` Jackson 2 mapper
  (`com.fasterxml.jackson.databind.ObjectMapper`). Spring Boot 4.1.1 HTTP auto-configuration targets
  Jackson 3 (`tools.jackson.databind.ObjectMapper`), which is type-incompatible with TelegramBots 10.3.0.
  This shared Jackson 2 bean is injected and reused across both outbound client (`OkHttpTelegramClient`)
  and long-polling updates (`TelegramBotsLongPollingApplication`) paths without modifying Boot's Jackson 3
  HTTP serialization.
- Constructor injection: Components (`TelegramEchoConsumer`, `TelegramBotLifecycle`) use Lombok
  `@RequiredArgsConstructor` for clean, final field-based dependency injection.
- Dedicated profile: `telegram-echo` excludes DataSource, JPA, Flyway, and Spring AI
  auto-configurations, allowing local execution without database or AI prerequisites.
  Unsafe dependency exception logging in `BotSession` is silenced via configuration
  to prevent token-bearing URLs from reaching application logs.
- Resource & Lifecycle ownership: `TelegramConfiguration` declares the `TelegramClient`,
  the owned outbound `OkHttpClient` transport bean (`telegramHttpClient`), a dedicated
  single-thread scheduler, and the native `TelegramBotsLongPollingApplication`.
  `TelegramBotLifecycle` manages registration and owns full resource cleanup: on startup failure,
  stop, or destruction, it releases the session, polling application, background scheduler,
  and HTTP transport. Transport cleanup requests cancellation of queued and in-flight calls via
  `dispatcher().cancelAll()`, shuts down the dispatcher `executorService()` so that new tasks are
  rejected and idle threads can exit, and evicts idle pooled sockets via `connectionPool().evictAll()`.
  Termination is not awaited (`awaitTermination` is not invoked), and cancellation timing, socket abort
  completion, and worker exit have not been measured or observed. Registration failures fail startup
  with sanitized diagnostics (`IllegalStateException` without cause) rather than swallowing errors.
- Threading architecture & execution model:
  - Scheduled daemon platform thread: `telegramPollingExecutor` creates a single daemon platform
    thread named `telegram-polling`. Manual thread factories are not auto-converted by Spring Boot.
  - Inline blocking callback and send: TelegramBots 10.3.0 `BotSession` performs long polling and
    invokes `TelegramEchoConsumer.consume` inline on that single scheduled platform worker. Inbound
    echo sends call `TelegramClient.execute()`, which blocks on `executeAsync().get()`. Therefore,
    outbound sends block the single scheduler thread, and slow sends delay subsequent updates and
    poll cycles.
  - Library-owned non-daemon dispatcher workers: OkHttp 4.12.0 `Dispatcher.executorService` utilizes
    `Util.threadFactory("OkHttp Dispatcher", false)`, spawning non-daemon platform workers for
    outbound call execution. Lifecycle release calls `dispatcher().cancelAll()` to request cancellation
    of calls and shuts down the dispatcher executor service so new tasks are rejected; termination is not
    awaited, cancellation/completion timing remains unobserved, and actual thread exit timing has not been
    measured at runtime.
  - Daemon HTTP housekeeping: OkHttp 4.12.0 `TaskRunner.INSTANCE` utilizes daemon platform threads
    for internal connection cleanup and task execution.
  - Update-ID reduction: `BotSession` update-ID reduction uses `parallelStream()`, which may employ
    common ForkJoin pool platform workers for CPU reduction.
  - Spring virtual thread opt-in: Java 25 public `Thread` constructors create platform threads. Spring
    Boot 4.1.1 `Threading.VIRTUAL.isActive` requires `spring.threads.virtual.enabled=true` (default `false`);
    no candidate YAML or launcher opt-in exists.
  - Architectural fit: The single-thread platform model fits this minimal serial echo route; no
    blanket virtual conversion, global virtual enablement, or asynchronous callback offload is
    introduced.
- Process lifetime & shutdown notes: Resource cleanup invokes `ExecutorService.shutdown()`,
  which requests an orderly shutdown of the background polling thread rather than forcibly aborting
  in-flight socket operations. In-place restarts within the same process lifetime are unsupported;
  the native library's single registration lifecycle and webhook removal behavior require fresh process
  startup.
- Echo behavior: `TelegramEchoConsumer` echoes incoming regular text messages to the
  originating chat and message thread/topic, preserving exact Unicode, whitespace, and line
  breaks without markup transformations. Non-text updates and empty messages are ignored.
  Error handling prevents leaking bot tokens or message content.
- Verification limits & operational notes: Unit tests were prepared and statically
  inspected with mocked native components. No Java runtime was available to run tests,
  and no live Telegram polling session, network connection, or live credentials were
  used or verified. Library source inspection indicates that starting long polling calls
  `deleteWebhook`; future manual runs require internet access to `api.telegram.org` and
  must not contend with an active webhook or another poller for the same token. All threading
  characteristics describe source-selected behavior; runtime threading execution, thread dumps,
  latencies, and transport cancellation timing remain unobserved.

Once an appropriate database and enabled provider configuration are supplied,
the launch command is:

```sh
sh ./mvnw spring-boot:run
```

Startup and authentication behavior must be observed in the configured
environment. There is no repository-provided complete local runtime recipe for the
default/general profile (with database and AI providers); for local development of the
Telegram echo route, a dedicated launcher recipe is provided by
[`scripts/dev-api.sh`](../../scripts/dev-api.sh) using the `telegram-echo` profile
(see [Root README](../../README.md#running-the-development-api-launcher-telegram-echo)).
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
