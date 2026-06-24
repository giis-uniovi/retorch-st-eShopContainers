# CLAUDE.md — eShopContainers E2E Test Suite

> **Maintenance rule:** Every time a file in this repository is created, modified, or deleted in a conversation with Claude, update this file to reflect the change before closing the task. Keep the sections below accurate and concise. Prefer recording *current state* and *non-obvious gotchas*; the change history itself lives in `git log`.

---

## Project overview

End-to-end test suite for the [eShopOnContainers](https://github.com/dotnet-architecture/eShopOnContainers) microservices reference application.
Tests cover the WebMVC and WebSPA frontends plus the REST APIs (Basket, Catalog, Ordering, Identity, Payment).
The project integrates with **Retorch** for parallel test orchestration and uses **Selenoid** for browser automation.

---

## Technology stack

### Test suite (Java — project root)

| Layer | Technology |
|---|---|
| Test framework | JUnit 5 (Jupiter) + Selenium 4 |
| Build | Maven 3 (Surefire 3.5.5) |
| Browser orchestration | Selenoid (`chrome-node`, `chrome-video`, `selenium-hub`) |
| Selema integration | `giis-uniovi/selema` (session/grid lifecycle) |
| CI / orchestration | Jenkins + Retorch (parallel TJob model) |
| Code quality | SonarLint, SpotBugs |

### SUT — eShopOnContainers microservices (`sut/src/`)

| Layer | Technology |
|---|---|
| Runtime | **.NET 10 LTS** (all 30 projects target `net10.0`) |
| SDK / runtime images | `mcr.microsoft.com/dotnet/sdk:10.0`, `mcr.microsoft.com/dotnet/aspnet:10.0` |
| Identity | Duende IdentityServer 8.0.1 |
| ORM | Entity Framework Core 10.0.9 |
| Reverse proxy / BFF | YARP 2.0.0 |
| gRPC | Grpc.AspNetCore 2.51.0 |
| Frontend (SPA) | **Angular 21 LTS** built with Node.js 22 (`node:22-bullseye`) |
| Message broker | RabbitMQ 3 (`rabbitmq:3-management-alpine`); client `RabbitMQ.Client` 7.2.1 (async `IChannel` API) |
| Databases | SQL Server 2019, MongoDB 8, Redis (alpine) |
| Logging | Seq (`datalust/seq:2025.2`) |
| API gateway | Envoy (`envoyproxy/envoy:v1.11.1`) |
| Package management | Central version management via `Directory.Packages.props` |

---

## Repository structure

```
.retorch/
  configurations/   resource model JSON
  envfiles/         one .env per TJob (tjoba.env … tjobp.env)
  scripts/
    coilifecycles/  coi-setup.sh, coi-teardown.sh
    tjoblifecycles/ tjob-setup.sh, tjob-testexecution.sh, tjob-teardown.sh
    monitoring/     (reserved — not yet wired into CI)
    printLog.sh, writetime.sh, storeContainerLogs.sh,
    savetjoblifecycledata.sh, waitforSUT.sh
monitoring/         standalone local resource-monitoring tool (see below)
src/test/java/giis/eshopcontainers/e2e/functional/
  common/           BaseLoggedClass, BaseWebSPALoggedClass, BaseAPIClass …
  tests/
    webmvc/         WebMVC*Tests
    webspa/         WebSPA*Tests
  utils/            Navigation*, Basket*, Orders*, Click, Waiter …
sut/src/            docker-compose.yml + microservice source (git submodule)
  Services/         Basket, Catalog, Identity, Ordering, Payment, Webhooks
  Web/              WebMVC, WebSPA, WebStatus, WebhookClient
  ApiGateways/      Envoy (mobile/web), Mobile.Bff.Shopping, Web.Bff.Shopping
  BuildingBlocks/   EventBus, EventBusRabbitMQ, IntegrationEventLogEF …
  Directory.Packages.props   central NuGet version management
Jenkinsfile         16 parallel TJobs across 3 stages
```

---

## SUT microservices

21 Docker services defined in `docker-compose.yml`:

| Service | Description | Port |
|---|---|---|
| `webmvc` | MVC web frontend | 5100 |
| `webspa` | Angular SPA | 5104 |
| `identity-api` | OAuth2 / OpenID Connect (Duende IS) | 5105 |
| `webshoppingagg` | Web BFF (YARP reverse proxy) | 5121 |
| `mobileshoppingagg` | Mobile BFF | — |
| `basket-api` | Shopping basket (Redis + gRPC) | — |
| `catalog-api` | Product catalog (SQL + gRPC) | — |
| `ordering-api` | Order management (SQL + gRPC) | — |
| `ordering-backgroundtasks` | Order background job | — |
| `ordering-signalrhub` | Real-time order updates (SignalR) | — |
| `payment-api` | Payment processing | 5108 |
| `webhooks-api` | Webhook management | — |
| `webhooks-client` | Webhook consumer test client | — |
| `webshoppingapigw` | Envoy API gateway (web) | — |
| `mobileshoppingapigw` | Envoy API gateway (mobile) | — |
| `webstatus` | Health check dashboard | 5107 |
| `seq` | Structured logging | 5340 |
| `sqldata` | SQL Server 2019 | 5433 |
| `nosqldata` | MongoDB 8 | — |
| `basketdata` | Redis | — |
| `rabbitmq` | RabbitMQ message broker | 15672 |

---

## Container / TJob naming

- In CI every Docker service is named `{service}_{tjobName}` (e.g. `webmvc_tjoba`).
- The `TJOB_NAME` env var is set by the `.retorch/envfiles/{tjob}.env` file and passed to Docker Compose via `${TJOB_NAME:-default}`.
- Locally the project name `-p local` names containers `{service}_local`.
- 16 TJobs total: `tjoba` … `tjobp`, spread across Stage 0 (a–f), Stage 1 (g–k), Stage 2 (l–p).

---

## Running tests locally (sequential)

> **Do NOT pass `-DSUT_URL`** when running locally. `BaseAPIClass` uses the presence of `SUT_URL` to switch to Docker-internal hostnames (`identity_api_local:80`) which are unreachable from the host. Without it, both browser tests and API tests fall back to the `LOCALHOST_*` URLs in `src/test/resources/test.properties`.

```bash
# 1. Start SUT (use redeploy-local.ps1 for the local docker-compose override)
.\redeploy-local.ps1          # full build
.\redeploy-local.ps1 -NoBuild # skip image rebuild

# 2. Run tests (all, or a subset)
mvn test -DTJOB_NAME=local
mvn test -DTJOB_NAME=local -Dtest=WebSPACatalogTests

# 3. Tear down
docker compose -f sut\src\docker-compose.yml `
  -f sut\src\docker-compose.local-override.yml `
  --env-file .retorch\envfiles\local.env `
  -p local down --volumes
```

Localhost endpoints after `redeploy-local.ps1`:

| Service | URL |
|---|---|
| WebMVC | http://localhost:5100 |
| WebSPA | http://localhost:5104 |
| Identity | http://localhost:5105 |
| BFF (webshoppingagg) | http://localhost:5121 |
| Payment | http://localhost:5108 |
| WebStatus | http://localhost:5107 |
| Seq (logs) | http://localhost:5340 |
| RabbitMQ | http://localhost:15672 (guest/guest) |
| SQL Server | localhost:5433 (sa/Pass@word) |

---

## Local resource monitoring

`monitoring/` records per-container CPU/Mem (SUT + Selenoid browsers) and produces an Excel report at `monitoring/data/resource-report.xlsx` (git-ignored). Python venv (`monitoring\.venv`) is created automatically on first run.

```powershell
# Recommended: full cycle (deploy SUT -> monitor -> run tests -> Excel report)
.\monitoring\run-local-suite.ps1
.\monitoring\run-local-suite.ps1 -NoBuild -Interval 10 -Test "WebSPACatalogTests"

# Manual control
.\monitoring\start-monitoring.ps1   # ... run tests ...   .\monitoring\stop-monitoring.ps1
python monitoring\generate-excel.py
```

Bash equivalents: `monitoring/run-with-monitoring.sh`, `start-monitoring.sh`, `stop-monitoring.sh`.
Report sheets: **Summary** (Avg/Max/Min/P95 CPU+Mem per container), **SUT Containers**, **Browsers**, **Raw Data**.

---

## Key classes (test suite)

| Class | Purpose |
|---|---|
| `BaseLoggedClass` | Base for all tests: browser lifecycle, login/logout, `clearUserBasket()` |
| `BaseWebSPALoggedClass` | Overrides URLs and helpers for the Angular SPA |
| `BaseAPIClass` | OAuth2 token acquisition and HTTP client setup for API tests |
| `BasketWebSPA` | SPA-specific basket helpers; owns `PAGER_INFO_LOCATOR` and `waitForPagerUpdate()` |
| `NavigationWebSPA` | SPA navigation (basket, orders, hover menus) |
| `Waiter` | `waitUntil()` wrapper with friendly timeout messages |
| `Click` | Safe click with implicit wait and stale-element retry |

---

## Development conventions

- **No comments** unless the _why_ is non-obvious (hidden constraint, bug workaround, subtle invariant).
- **Catch only checked exceptions** — `catch (IOException | JsonParseException e)` not `catch (Exception e)`.
- **CSS class names** used as Selenium locators must have a comment referencing the template file and element.
- **JWT guards** — `jwtParts.length != 3` and `!payloadObj.has("sub")` checks must precede JWT indexing.
- **`set -e`** is active in all lifecycle shell scripts; new scripts follow the same convention.
- Prefer editing existing files; avoid new abstractions unless the same logic appears in three or more places.
- **`-DSUT_URL` must not be passed** for local test runs (see "Running tests locally" above).

---

## Gotchas & non-obvious constraints

- **RabbitMQ.Client / healthcheck coupling**: `RabbitMQ.Client` 7.x removed the sync `IModel` API in favor of async `IChannel`. `AspNetCore.HealthChecks.Rabbitmq` must be >= 9.0 (built against `RabbitMQ.Client` >= 7.0) or the RabbitMQ healthcheck throws `TypeLoadException: Could not load type 'RabbitMQ.Client.IModel'` at *runtime* (builds fine, deploy fails). Bump both together; see `CommonExtensions.AddDefaultHealthChecks` (uses `ConnectionFactory.CreateConnectionAsync()`) and `BuildingBlocks/EventBus/EventBusRabbitMQ/*`.
- **Duende.IdentityServer major bumps**: add a required `CancellationToken` param to most `IIdentityServerInteractionService`/`IDeviceFlowInteractionService`/`IEventService.RaiseAsync`/`IProfileService`/`IClientStoreExtensions`/`IResourceStore` methods, and rename `AuthorizationError` → `InteractionError`. Check every controller under `Services/Identity/Identity.API/Quickstart/` when bumping (pass `HttpContext.RequestAborted`).
- **WebSPA Node image**: `docker-compose.yml`'s `NODE_IMAGE` build arg (currently `node:22-bullseye`) overrides the WebSPA Dockerfile's own `ARG` default — update both when bumping Node.
- **Angular 21 `type="module"` deferred loading**: HTTP responses arriving during app init run outside Zone.js's change-detection zone. Components that mutate state in `ngOnInit`/subscribe callbacks need an injected `ChangeDetectorRef` + `detectChanges()` (see `catalog`, `basket`, `basket-status`, `orders`, `orders-detail` components).
- **`angular.json` `outputPath`** must be the object form `{"base":"../wwwroot","browser":""}`, not a string — with the `application` builder a string path nests output under `wwwroot/browser/` and breaks .NET static file serving.
- **WebSPA `package-lock.json`**: if `npm install` fails with `ERESOLVE` on `@angular/*` peer deps (lockfile drift vs. `^21.x` ranges), delete `Client/package-lock.json` and regenerate with a clean `npm install` inside `node:22-bullseye`.
- **Known unfixed CVE**: `webpack-dev-server <=5.2.3` (GHSA-79cf-xcqc-c78w, Moderate, dev-only). Full CVE backlog in `SECURITY_TODO.md` (project root).
- **Dependabot** (`.github/dependabot.yml`): nuget `ignore` rules block semver-major bumps for `Microsoft.AspNetCore.*`/`Microsoft.EntityFrameworkCore.*`/`Microsoft.Extensions.*`/`Microsoft.NET.*`/`System.*`/`@angular/*` — these require coordinated manual upgrades (see gotchas above). `groups` keep `Duende.IdentityServer*`+`Microsoft.IdentityModel.*` and `RabbitMQ.Client`+`AspNetCore.HealthChecks.Rabbitmq` bumped together.
- **`yarn.lock` removed** — stale (frozen since initial commit), unused (build only runs `npm install`); was causing 79 phantom Dependabot alerts for a 3-year-old dependency tree that doesn't exist in the built image. `npm audit` on the live `package-lock.json` now reports 0 vulnerabilities.
- **SonarCloud** (`sonar.properties` + `.github/workflows/build.yml`): analyzes the SUT only (C# + TypeScript/Angular WebSPA), scoped to `./sut` via `sonar.projectBaseDir` so repo-root files and `.github/` workflows are excluded — paths in `sonar.properties` (e.g. `tsconfigPaths`) are therefore relative to `sut/`. `projectBaseDir` is set in the workflow as an **absolute** path (`$GITHUB_WORKSPACE/sut`), not in `sonar.properties`: the .NET scanner fails post-processing with "project base directory doesn't exist" if it is relative. C# requires `dotnet-sonarscanner begin/end` wrapping `dotnet build` (Roslyn interceptors). The config file is named `sonar.properties`, **not** `sonar-project.properties` — the SonarScanner for .NET aborts post-processing if a file with that reserved name exists in the repo. The workflow reads `sonar.properties` line-by-line into `/d:` flags on `begin`; `projectKey`/`projectName`/`organization` must use the dedicated `/k:`/`/n:`/`/o:` flags (scanner v11 rejects them as `/d:` properties), and `sonar.token` is read from the `SONAR_TOKEN` env var (not expanded into the run block). Do NOT replace `dotnet sonarscanner end` with `SonarSource/sonarqube-scan-action`.
