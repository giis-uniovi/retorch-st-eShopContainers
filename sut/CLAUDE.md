# CLAUDE.md — eShopContainers E2E Test Suite

> **Maintenance rule:** whenever a file in this repo is created, modified, or deleted in a conversation with Claude, update this file before closing the task. Record *current state* and *non-obvious gotchas* only — change history lives in `git log`.

## Project overview

E2E test suite for the [eShopOnContainers](https://github.com/dotnet-architecture/eShopOnContainers) microservices app, covering the WebMVC and WebSPA frontends and the REST APIs (Basket, Catalog, Ordering, Identity, Payment). Uses **Retorch** for parallel test orchestration (Jenkins) and **Selenoid** for browser automation.

## Technology stack

**Test suite (Java, project root):** JUnit 5 + Selenium 4, Maven 3 (Surefire 3.5.5), Selenoid (`chrome-node`/`chrome-video`/`selenium-hub`), `giis-uniovi/selema`, Jenkins + Retorch, SonarLint/SpotBugs.

**SUT (`sut/src/`):**

| Layer | Technology |
|---|---|
| Runtime | **.NET 10 LTS** (`net10.0`); images `mcr.microsoft.com/dotnet/{sdk,aspnet}:10.0` |
| Identity | Duende IdentityServer 8.0.2 |
| ORM | EF Core 10.0.9 |
| BFF / gateway | YARP 2.3.0; Envoy `v1.11.1` |
| gRPC | Grpc.AspNetCore 2.51.0 |
| SPA | **Angular 21 LTS** (21.2.x), Node 22 (`node:22-bullseye`), TypeScript ~5.9, ng-bootstrap 20 (pinned) |
| Messaging | RabbitMQ 3 (`rabbitmq:3-management-alpine`); `RabbitMQ.Client` 7.2.1 (async `IChannel` API) + `AspNetCore.HealthChecks.*` 9.0.0 |
| Data | SQL Server 2019, MongoDB 8, Redis (alpine) |
| Logging | Seq (`datalust/seq:2025.2`) |
| OpenAPI | Swashbuckle 6.9.0 (pinned, see gotchas) |
| NuGet | Central version management: `sut/src/Directory.Packages.props` (transitive pinning enabled) |

## Repository structure

```
.retorch/
  configurations/   resource model JSON
  envfiles/         one .env per TJob (tjoba.env … tjobp.env, local.env)
  scripts/          coilifecycles/, tjoblifecycles/, waitforSUT.sh, printLog.sh, …
src/test/java/giis/eshopcontainers/e2e/functional/
  common/           BaseLoggedClass, BaseWebSPALoggedClass, BaseAPIClass …
  tests/            webmvc/ (WebMVC*Tests), webspa/ (WebSPA*Tests)
  utils/            Navigation*, Basket*, Orders*, Click, Waiter …
sut/src/            docker-compose.yml + microservice source
  Services/         Basket, Catalog, Identity, Ordering, Payment, Webhooks
  Web/              WebMVC, WebSPA, WebStatus, WebhookClient
  ApiGateways/      Envoy configs, Mobile.Bff.Shopping, Web.Bff.Shopping
  BuildingBlocks/   EventBus, EventBusRabbitMQ, IntegrationEventLogEF …
Jenkinsfile         16 parallel TJobs across 3 stages
redeploy-local.ps1 / .sh   local SUT deployment helpers
```

## SUT microservices (21 compose services)

Host-mapped ports (local deploy): `webmvc` 5100, `webspa` 5104, `identity-api` 5105 (Duende), `webstatus` 5107 (healthcheck dashboard), `payment-api` 5108, `webshoppingagg` 5121 (web BFF), `seq` 5340, `sqldata` 5433 (sa/Pass@word), `rabbitmq` 15672 (guest/guest).

Internal-only: `mobileshoppingagg`, `basket-api` (Redis+gRPC), `catalog-api` (SQL+gRPC), `ordering-api` (SQL+gRPC), `ordering-backgroundtasks`, `ordering-signalrhub`, `webhooks-api`, `webhooks-client`, `webshoppingapigw`/`mobileshoppingapigw` (Envoy), `nosqldata` (Mongo), `basketdata` (Redis).

## Container / TJob naming

- CI names every service `{service}_{tjobName}` (e.g. `webmvc_tjoba`) via `TJOB_NAME` from `.retorch/envfiles/{tjob}.env`; locally `-p local` yields `{service}_local`.
- 16 TJobs `tjoba`…`tjobp`: Stage 0 (a–f), Stage 1 (g–k), Stage 2 (l–p).

## Running tests locally (sequential)

> **Do NOT pass `-DSUT_URL`** locally. Its presence makes `BaseAPIClass` use Docker-internal hostnames (`identity_api_local:80`), unreachable from the host. Without it, tests use the `LOCALHOST_*` URLs from `src/test/resources/test.properties`.

```powershell
.\redeploy-local.ps1            # build images + deploy (add -NoBuild to skip rebuild)
mvn test -DTJOB_NAME=local      # all tests, or add -Dtest=WebSPACatalogTests
# teardown:
docker compose -f sut\src\docker-compose.yml -f sut\src\docker-compose.local-override.yml `
  --env-file .retorch\envfiles\local.env -p local down --volumes
```

## Key classes (test suite)

| Class | Purpose |
|---|---|
| `BaseLoggedClass` | Browser lifecycle, login/logout, `clearUserBasket()` |
| `BaseWebSPALoggedClass` | SPA URL/helper overrides |
| `BaseAPIClass` | OAuth2 token + HTTP client for API tests |
| `BasketWebSPA` | SPA basket helpers; `PAGER_INFO_LOCATOR`, `waitForPagerUpdate()` |
| `NavigationWebSPA` | SPA navigation (basket, orders, hover menus) |
| `Waiter` / `Click` | `waitUntil()` with friendly timeouts / safe click with stale-element retry |

## Development conventions

- No comments unless the *why* is non-obvious (hidden constraint, workaround, invariant).
- Catch only checked exceptions — `catch (IOException | JsonParseException e)`, never `catch (Exception e)`.
- CSS-class Selenium locators need a comment referencing the template file/element.
- JWT guards (`jwtParts.length != 3`, `!payloadObj.has("sub")`) must precede JWT indexing.
- `set -e` is active in all lifecycle shell scripts.
- Prefer editing existing files; no new abstractions unless logic repeats 3+ times.

## Gotchas & version constraints

- **RabbitMQ.Client / healthcheck coupling**: `RabbitMQ.Client` 7.x replaced sync `IModel` with async `IChannel`. `AspNetCore.HealthChecks.Rabbitmq` must stay >= 9.0 or the healthcheck throws `TypeLoadException: ...IModel` at *runtime* (build passes, deploy fails). Bump together (Dependabot group). See `CommonExtensions.AddDefaultHealthChecks` and `BuildingBlocks/EventBus/EventBusRabbitMQ/*`.
- **Swashbuckle pinned at 6.x**: 7+ pulls Microsoft.OpenApi 2.x whose `OpenApiSecurityScheme`/`OpenApiReference`/`OpenApiResponse` API breaks `Services.Common` (`AuthorizeCheckOperationFilter.cs`, `CommonExtensions.cs`). Semver-major blocked in Dependabot; keep `Swashbuckle.AspNetCore` + `.Newtonsoft` on the same version.
- **ng-bootstrap pinned at 20.x**: v21 requires Angular 22 peer, but Angular is pinned to 21 LTS. Semver-major blocked in Dependabot; unlock only when Angular moves to 22.
- **Duende.IdentityServer major bumps** add required `CancellationToken` params to interaction/event/store service methods and renamed `AuthorizationError` → `InteractionError`; touch every controller under `Identity.API/Quickstart/` (pass `HttpContext.RequestAborted`).
- **WebSPA Node image**: `docker-compose.yml`'s `NODE_IMAGE` build arg overrides the WebSPA Dockerfile `ARG` default — update both when bumping Node.
- **Angular 21 `type="module"` loading**: HTTP responses during app init run outside Zone.js; components mutating state in `ngOnInit`/subscribe callbacks need `ChangeDetectorRef.detectChanges()` (`catalog`, `basket`, `basket-status`, `orders`, `orders-detail`).
- **`angular.json` `outputPath`** must be the object form `{"base":"../wwwroot","browser":""}` — a plain string nests output under `wwwroot/browser/` and breaks .NET static file serving.
- **npm CVE handling**: the `overrides` block in `Client/package.json` pins vulnerable transitives (incl. `esbuild >=0.28.1`, `webpack-dev-server >=5.2.4`, `@babel/core >=7.29.6 <8`); `npm audit` is currently clean. On `ERESOLVE` failures from lockfile drift, delete `package-lock.json` and regenerate with a clean `npm install` (Node 22).
- **No `yarn.lock`**: deliberately deleted (build uses npm only). Do not reintroduce — GitHub scans any lockfile and a stale one generated ~80 phantom Dependabot alerts.
- **Dependabot** (`.github/dependabot.yml`): semver-major blocked for `Microsoft.AspNetCore/EntityFrameworkCore/Extensions/NET.*`, `System.*`, `Swashbuckle.AspNetCore*`, `@angular/*`, `@angular-devkit/*`, `@ng-bootstrap/ng-bootstrap` (+ typescript minor); groups keep Duende+IdentityModel and RabbitMQ.Client+HealthChecks.Rabbitmq in lock-step.
