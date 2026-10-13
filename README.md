# LifeOS

LifeOS is a personal life assistant under development. The intended product
uses Telegram and AI to help manage habits, tasks, finances, goals, and reports.
The [product requirements](docs/LifeOS_PRD.md) describe that planned behavior;
they are not an inventory of implemented features.

## Current implementation

The repository contains a Spring Boot backend in `apps/api/` targeting Java 25 and
Spring Boot 4.1.1. Implementation and offline tests for a minimal, plain-text
Telegram echo route with configured sender-admission control have been accepted.

### Telegram echo integration (`telegram-echo` profile)

The minimal echo route is implemented in `apps/api/` and managed locally via root
development scripts:

- **Echo contract**: Incoming regular text messages produce an exact text echo in
  the originating chat (and message thread/topic when provided), preserving Unicode,
  whitespace, and line breaks without markup transformations. Non-text updates and
  empty messages are ignored.
- **Credential & registration rule**: `TELEGRAM_BOT_TOKEN` is loaded from the root `.env`
  file and bound to `lifeos.telegram.bot-token`. Under the active `telegram-echo` profile,
  a missing or blank token fails fast with zero bot registration, network calls, or
  polling sessions, while the root development launcher also validates and fails early if
  the token is missing or blank. Real credentials must never be committed.
- **Dedicated profile**: The `telegram-echo` Spring profile isolates the echo flow,
  excluding DataSource, JPA, Flyway migrations, and Spring AI auto-configurations so
  that local development requires no database, vector store, or model provider.
- **Access control contract**:
  - *Modes*: Configured via `TELEGRAM_ACCESS_MODE` and bound to `lifeos.telegram.access-mode`.
    Supported values are `WHITELIST` and `PUBLIC` (case-sensitive). Missing, blank, or
    unrecognized modes cause early startup/launcher failure; `PUBLIC` is never inferred from
    an empty list or missing mode.
  - *Allowed user IDs*: Configured via `TELEGRAM_ALLOWED_USER_IDS` and bound to
    `lifeos.telegram.allowed-user-ids`. Values must be a comma-separated list of positive
    decimal Telegram sender IDs within the signed 64-bit Long range (1 to 9223372036854775807).
    Surrounding entry whitespace is trimmed (supporting multiline entries); signs, zero,
    non-digits, overflow, empty elements, and trailing separators are rejected.
  - *Authorization rules*: In `WHITELIST` mode, a non-empty list of valid IDs is strictly
    required and only senders in the whitelist are admitted. In `PUBLIC` mode, any identified
    sender with a valid positive ID is admitted; an empty or absent list is valid only with
    explicit `PUBLIC` (any provided non-empty list is validated for syntax but does not
    restrict access).
  - *Identity resolution & reply routing*: Identity is resolved strictly from `message.from.id`
    (positive Telegram sender User ID). Missing sender or non-positive sender ID is denied in
    both modes. Never authorizes via chat ID, username, or message text; chat ID is used only as
    the outbound reply address. Denied updates produce no response or outbound client send call.
    Authorized messages retain exact plain-text echo behavior.
  - *Implementation and verification status*: Root access configuration templates and launcher
    pre-launch validators have been accepted by Lead (verified across 32 pure parser test cases).
    Backend sender-admission implementation and 79 focused offline tests in `apps/api/` have been
    accepted by Lead on Amazon Corretto 25.0.4 and cached Maven 3.10.0 (including isolated
    constructor selection and bean factory mock wiring without application context refresh/start).
    No User persistence, onboarding, AI, or database additions exist. Live admission verification,
    live transport, and live Telegram bot polling remain unexercised.
- **Component wiring & Jackson compatibility**: Components use Lombok `@RequiredArgsConstructor`
  for constructor-based dependency injection. Because Spring Boot 4.1.1 auto-configures
  Jackson 3 (`tools.jackson.databind.ObjectMapper`) while the TelegramBots 10.3.0 library
  requires Jackson 2 (`com.fasterxml.jackson.databind.ObjectMapper`), an explicitly owned
  shared Jackson 2 `ObjectMapper` bean is injected into the Telegram client and polling
  application without interfering with Boot's HTTP serialization. See the
  [API README](apps/api/README.md) for detailed configuration, properties, and component internals.
- **Threading architecture & execution model**:
  - *Virtual threads*: Spring Boot 4.1.1 virtual thread execution defaults to disabled
    (`spring.threads.virtual.enabled=false`), and no opt-in is configured; Java 25 public
    `Thread` constructors create platform threads.
  - *Platform threading*: Long-polling runs on a repository-created Spring scheduler bean
    (`telegramPollingExecutor`), providing a library-used single daemon platform worker thread
    named `telegram-polling`. Outbound HTTP sends use OkHttp's library-owned platform worker threads.
  - *Serial execution & blocking sends*: TelegramBots 10.3.0 invokes update consumption inline
    on the single polling thread, and outbound message dispatch synchronously blocks on the
    underlying HTTP request (`executeAsync().get()`). Consequently, slow outbound sends delay
    subsequent update processing and polling cycles.
  - *Orderly shutdown*: On application context close or shutdown, cancellation of queued and
    in-flight calls is requested via `dispatcher().cancelAll()` and executor shutdown is initiated,
    but termination is not awaited (`awaitTermination` is not invoked), and cancellation timing has
    not been measured at runtime.
  - *Source-selected description & offline observation*: All threading characteristics describe
    source-selected configuration and library design. An isolated offline scheduler test
    creates the actual repository scheduler and executes a harmless task, observing that the
    worker thread is real, named `telegram-polling`, daemon, and not virtual (`isVirtual=false`),
    without using actual polling, sessions, or transport. Live bot thread allocation, network
    latencies, and cancellation timing remain unobserved.

> [!NOTE]
> **Static inspection, access control, and runtime verification status**:
> Root access configuration templates (`.env.example`), ignore rules (`.gitignore`), and the
> development launcher (`scripts/dev-api.sh`) pre-launch access validators have been accepted
> by Lead (independently verified across 32 pure parser test cases without launcher or
> environment sourcing, including multiline and separator edge cases).
>
> On the backend, configured sender-admission implementation and 79 focused offline tests in
> `apps/api/` (spanning 9 main and 7 test sources, constructor selection, and bean factory
> mock wiring without application context refresh or start) have been accepted by Lead on
> Amazon Corretto 25.0.4 and cached Maven 3.10.0 (0 failures, 0 errors, 0 skips). No User
> persistence, onboarding, AI, or database additions exist.
>
> Historically, in earlier agent verification, Java/Javac was unavailable so automated builds
> and tests were not run at that time. Subsequently, an operator reported successful manual
> execution of `scripts/dev-api.sh` after sourcing SDKMAN environment initialization in their
> shell, observing that sending literal `A` returned literal `A`. This is an operator-reported
> manual observation rather than agent-executed validation (and does not prove newly added
> access admission boundaries); exact operator initialization command/path, JDK version,
> executed revision, and build outputs were not supplied. Full Maven packaging (`package`),
> full `@SpringBootTest` context execution, formatter checks, live access admission
> verification, live transport/concurrency, and live shutdown/cancellation timing remain
> unexercised and unverified.

### Planned product capabilities

There are currently no application REST controllers, domain entities, repositories,
Flyway database migrations, or AI agent tools. Habits, tasks, finances, goals, reminders,
analytics, and Excel export remain planned product requirements. The React dashboard/Telegram
Mini App (`apps/web/`) and Docker infrastructure (`infra/`) also remain planned.

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

### Running the development API launcher (telegram-echo)

A development launcher is provided at `scripts/dev-api.sh` to start the backend
with the dedicated `telegram-echo` Spring profile:

1. **Configure local environment**:
   Edit the provided root `.env` file directly to set your bot token and access controls:
   ```sh
   TELEGRAM_BOT_TOKEN='your_bot_token_here'
   TELEGRAM_ACCESS_MODE='WHITELIST'
   TELEGRAM_ALLOWED_USER_IDS='100000001,100000002'
   ```
   If `.env` is absent, create it using a non-overwriting copy:
   ```sh
   cp -n .env.example .env
   ```
   Never overwrite an existing `.env` file or pre-existing configuration values.
2. **Configuration and credential requirements**:
   - `TELEGRAM_BOT_TOKEN` must be set to a valid bot token obtained from
     [@BotFather](https://t.me/botfather). The launcher validates that the token is
     non-blank and fails early if it is missing or empty.
   - `TELEGRAM_ACCESS_MODE` must be configured as either `WHITELIST` or `PUBLIC` (case-sensitive).
   - `TELEGRAM_ALLOWED_USER_IDS` must contain comma-separated positive decimal 64-bit Long IDs
     (required and non-empty when mode is `WHITELIST`, optional when mode is `PUBLIC`).
   All pre-launch validation errors report variable names only, without revealing configured values.
3. **Prerequisites**:
   - Bash shell.
   - JDK 25 target (or compatible newer major): both runtime (`java`) and compiler
     (`javac`) available under `JAVA_HOME` or on `PATH`.
   - *SDKMAN-managed JDKs (optional)*: If using an SDKMAN-managed JDK, the same shell
     needs SDKMAN initialization to expose the installed JDK via `JAVA_HOME` and `PATH`
     (verified default-layout example: `source "$HOME/.sdkman/bin/sdkman-init.sh"`, adapt
     for your actual installation path). SDKMAN is an option for managing JDKs, not
     mandatory for all environments.
4. **Execution**:
   - When running from the repository root:
     ```sh
     ./scripts/dev-api.sh
     ```
   - When running from any other directory, invoke the script using its actual path:
     ```sh
     /path/to/lifeos/scripts/dev-api.sh
     ```

**Launcher operation, environment parsing, and precedence**:
- **Parsing and precedence**: `scripts/dev-api.sh` resolves the repository root reliably
  from its own script location and sources root `.env` directly in Bash with automatic
  variable export enabled (`set -a`). Root `.env` assignments become exported variables
  in the child process environment. Unrelated inherited environment variables remain intact
  in the child process. Sourcing `.env` does not alter parent shell state or rewrite
  values in files on disk. The `.env` file must contain trusted, Bash-compatible
  assignments (`KEY='value'`).
- **Diagnostic suppression & process security**: Command tracing is disabled and stderr
  is suppressed during `.env` sourcing to prevent Bash interpreter diagnostics from
  leaking sensitive values into trace or terminal logs. The bot token is passed to the
  child process strictly via the exported environment (`TELEGRAM_BOT_TOKEN`), never via
  JVM command-line system properties (`-D...`) or process arguments.
- **Maven execution**: The launcher enters `apps/api` and invokes the fixed launch command:
  `exec sh ./mvnw spring-boot:run -Dspring-boot.run.profiles=telegram-echo`.
  Unrestricted argument passthrough forwarding is omitted to preserve the fixed
  `telegram-echo` profile route. First Maven use will download Maven 3.10.0 and project
  dependencies from Maven repositories.
- **Status**: Root environment templates and launcher access validators have been accepted by
  Lead following pure parser validation across 32 synthetic cases without launcher/env sourcing.
  Configured sender-admission implementation and 79 focused offline tests in `apps/api/` have
  been accepted by Lead under offline compilation on Amazon Corretto 25.0.4 and cached Maven 3.10.0
  (0 failures, 0 errors, 0 skips, including constructor selection and bean factory mock wiring
  without application context refresh/start).
  Separately, earlier historical agent verification lacked Java, leaving automated suites unrun at
  that stage. An operator subsequently reported manual launch of `scripts/dev-api.sh` (after shell
  SDKMAN initialization) with literal `A` -> `A` echo verified live (not proving the newly added
  admission boundaries); exact operator revision, JDK version, and build logs were not reported.
  Full packaging (`package`), complete `@SpringBootTest` context loads, code formatting tasks,
  live access admission boundaries, live transport/concurrency, and runtime shutdown timing remain
  unexercised and unverified.

## Repository guidance

```text
lifeos/
├── .agents/skills/       # Repository-owned context and Java style skills
├── apps/api/            # Backend scaffold, Maven Wrapper, API guidance
├── docs/
│   ├── LifeOS_PRD.md     # Intended product behavior
│   └── WORKSPACE_PROTOCOL.md
├── scripts/
│   └── dev-api.sh       # Local development API launcher (telegram-echo)
├── .env.example         # Environment configuration template
├── .gitignore           # Root Git ignore rules (protects .env)
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
