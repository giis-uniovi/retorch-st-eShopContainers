# CLAUDE.md — eShopContainers E2E Test Suite

> **Maintenance rule:** Every time a file in this repository is created, modified, or deleted in a conversation with Claude, update this file to reflect the change before closing the task. Keep the sections below accurate and concise.

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
| Identity | Duende IdentityServer 7.0.0 |
| ORM | Entity Framework Core 10.0.0 |
| Reverse proxy / BFF | YARP 2.0.0 |
| gRPC | Grpc.AspNetCore 2.51.0 |
| Frontend (SPA) | **Angular 21 LTS** built with Node.js 22 (node:22-bullseye) |
| Message broker | RabbitMQ 3 (`rabbitmq:3-management-alpine`) |
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

The `monitoring/` folder contains a standalone tool that records CPU and memory
consumption of every running Docker container (SUT microservices + Selenoid browsers)
and produces an Excel report.

### All-in-one local runner (recommended)

```powershell
# Full cycle: venv setup → deploy SUT → monitor → run tests → Excel report
.\monitoring\run-local-suite.ps1

# Skip docker image rebuild if SUT images are already current
.\monitoring\run-local-suite.ps1 -NoBuild

# Run only a specific test class with a 10 s sampling interval
.\monitoring\run-local-suite.ps1 -NoBuild -Interval 10 -Test "WebSPACatalogTests"
```

The Python venv (`monitoring\.venv`) is created automatically on first run.
`redeploy-local.ps1` is called internally; its interactive `Read-Host` prompt was removed so it can be driven non-interactively.
Maven runs from the project root (not `monitoring/`), and `monitoring\run-local-suite.ps1` does NOT pass `-DSUT_URL` so API tests use localhost URLs.

### Manual one-shot wrapper

```powershell
# Install dependency once
pip install -r monitoring\requirements.txt

# Run the whole test suite with monitoring (PowerShell — Windows)
.\monitoring\run-with-monitoring.ps1 -Command "mvn test -DTJOB_NAME=local"

# Custom sampling interval (10 s instead of the default 5 s)
.\monitoring\run-with-monitoring.ps1 -Interval 10 -Command "mvn test -DTJOB_NAME=local -Dtest=WebSPACatalogTests"
```

```bash
# Bash equivalent (Git Bash / WSL / Linux)
bash monitoring/run-with-monitoring.sh "mvn test -DTJOB_NAME=local"
bash monitoring/run-with-monitoring.sh 10 "mvn test -DTJOB_NAME=local -Dtest=WebSPACatalogTests"
```

The Excel report is written to `monitoring/data/resource-report.xlsx`.

### Manual start / stop

```powershell
# PowerShell
.\monitoring\start-monitoring.ps1          # default 5 s interval
# … run tests …
.\monitoring\stop-monitoring.ps1
python monitoring\generate-excel.py
```

```bash
# Bash
bash monitoring/start-monitoring.sh
# … run tests …
bash monitoring/stop-monitoring.sh
python3 monitoring/generate-excel.py
```

### Excel sheets

| Sheet | Content |
|---|---|
| Summary | Avg / Max / Min / P95 CPU % and Mem (MiB) for every container |
| SUT Containers | Same, filtered to non-browser containers |
| Browsers | `chrome-node`, `chrome-video`, `selenium-hub` |
| Raw Data | Every measurement row (auto-filter enabled) |

`monitoring/data/` is git-ignored.

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
- **`-DSUT_URL` must not be passed** for local test runs — it switches `BaseAPIClass` to Docker-internal hostnames.

---

## Recent significant changes

### Branch `ft-updating-SUT-last-LTS-version-dot-net-and-security`

**SUT migrated to .NET 10 LTS** (from .NET 9):
- All 30 `.csproj` files, all 27 Dockerfiles, `Directory.Packages.props`.
- `Duende.IdentityServer` 6.2.3 → 7.0.0 (targets net8.0+, forward-compatible with .NET 10).
- `AspNetCore.HealthChecks.*` kept at 6.x (8.x had transitive `Azure.Identity` conflicts under `CentralPackageTransitivePinningEnabled`).
- `Swashbuckle.AspNetCore` 6.5.0 → 6.9.0; `Microsoft.AspNetCore.OpenApi` removed from `Services.Common.csproj` (Swashbuckle 6.x and Microsoft.OpenApi 2.x conflict — CS7069).
- `Microsoft.NET.Test.Sdk` 17.5.0 → 17.12.0; `Newtonsoft.Json` 13.0.2 → 13.0.3; `Microsoft.Build.Utilities.Core` 17.4.0 → 17.14.28 (transitive requirements from EFCore.Design 10.0.0).
- `Azure.Identity` 1.11.4 → 1.14.2 (EFCore.SqlServer 10.0.0 → Microsoft.Data.SqlClient 6.1.1 chain).
- `RedisBasketRepository.cs` line 38: added explicit `(string)data` cast on `JsonSerializer.Deserialize` call (.NET 10 added `ReadOnlySpan<byte>` overload causing CS0121 ambiguity with `RedisValue` implicit conversions).
- Node.js 16 → 22 in WebSPA Dockerfile; removed pinned `npm@9.5.1`.
- **`sut/src/docker-compose.yml` line 443**: `NODE_IMAGE` default updated `node:16-bullseye` → `node:22-bullseye` (this compose arg overrides the Dockerfile's own ARG default, so updating only the Dockerfile was insufficient).

**WebSPA Angular 21 security upgrade** (60+ Dependabot CVEs addressed):
- `package.json`: Angular 15.2.0 → ^21.0.0; zone.js ~0.11.4 → ~0.15.0; TypeScript 4.9.5 → ~5.9.0 (Angular 21.2.x requires >=5.9); rxjs → ^7.8.0; `@ng-bootstrap/ng-bootstrap` 14.0.1 → ^20.0.0 (ng-bootstrap 20 = Angular 21).
- `@angular/platform-server` removed (SPA — no SSR; eliminates all `@angular/platform-server` SSRF alerts).
- Removed deprecated devDependencies: `handlebars`, `lodash`, `eslint`, `rxjs-compat`, `protractor`, `codelyzer`, `tslint`, `rxjs-tslint`, `sass-lint`, `acorn`, `acorn-dynamic-import`, `file-loader`, `url-loader`, `webpack` (managed by Angular CLI internally).
- Added comprehensive `overrides` block for all vulnerable transitive packages (form-data, tar, node-forge, minimatch, immutable, flatted, semver, serialize-javascript, path-to-regexp, picomatch, brace-expansion, qs, uuid, fast-xml-parser, postcss, ajv, follow-redirects, tmp, nth-check, xml2js, js-yaml, ws, http-proxy-middleware, on-headers, @tootallnate/once, webpack).
- `angular.json`: migrated from `@angular-devkit/build-angular:browser` to `:application` builder; `polyfills` → `["zone.js"]`; `main` → `browser`; `browserTarget` → `buildTarget`; added `stylePreprocessorOptions.includePaths: ["."]`.
- `tsconfig.json`: removed `"enableIvy": false` (invalid since Angular 13); `moduleResolution` → `"bundler"`.
- `tsconfig.app.json`: removed `polyfills.ts` from `files` (zone.js now in angular.json).
- All 12 Angular components/directives/pipes: added `standalone: false` (Angular 19+ defaults to `standalone: true`; NgModule apps must opt out).
- All 12 SCSS files: `@import` → `@use ... as *` (Dart Sass 3.x removed global `@import`).
- `_button.scss`, `_form.scss`, `_toastr.scss`, `_utilities.scss`: added `@use 'variables' as *` (Dart Sass scopes variables per-module).
- `_toastr.scss`: migrated deprecated global Sass builtins → `sass:string` and `sass:list` modules.
- `globals.scss`: Bootstrap imported without `as *` (eliminated `$font-weight-normal` name clash); `_bootstrap-overrides.scss` removed (Bootstrap variable overrides require `with` config in `@use`, not a separate file).
- `yarn.lock` deleted; `package-lock.json` regenerated via clean `npm install` in `node:22-bullseye`.
- **Angular 21 runtime fixes for ES module (`type="module"`) deferred loading**:
  - `configuration.service.ts`: `Subject` → `ReplaySubject(1)` so late subscribers (DI-constructed after HTTP response arrives) still receive the `settingsLoaded$` event.
  - `catalog.component.ts`: injected `ChangeDetectorRef`; added `cdr.detectChanges()` in `getCatalog`, `getBrands`, `getTypes` subscribe callbacks.
  - `basket-status.component.ts`: injected `ChangeDetectorRef`; added `cdr.detectChanges()` in all `this.badge = ...` assignments inside subscribe callbacks.
  - `basket.component.ts`: injected `ChangeDetectorRef`; added `cdr.detectChanges()` in `ngOnInit` basket load subscribe callback.
  - `orders.component.ts`: injected `ChangeDetectorRef`; added `cdr.detectChanges()` in `getOrders` subscribe callback.
  - `orders-detail.component.ts`: injected `ChangeDetectorRef`; added `cdr.detectChanges()` in `getOrder` subscribe callback.
  - Root cause: Angular 21 uses `type="module"` (ES deferred execution) for script loading. HTTP callbacks triggered during Angular's initialization phase run outside Zone.js's change detection context; `detectChanges()` forces the template to re-render after each response.
- `angular.json` `outputPath` changed from `"../wwwroot"` (string) to `{"base":"../wwwroot","browser":""}` object — with the `application` builder, a string outputPath puts browser files in `wwwroot/browser/` (breaking .NET static file serving), but the object form puts them directly in `wwwroot/`.
- **Remaining known vulnerability (no upstream fix)**: `webpack-dev-server <=5.2.3` (GHSA-79cf-xcqc-c78w, Moderate, dev-only).
- Full CVE backlog tracked in `SECURITY_TODO.md` (project root).

**`run-local-suite.ps1` bug fixes**:
- Removed `-DSUT_URL` from Maven args (API tests must use `LOCALHOST_*` properties, not Docker-internal hostnames).
- Wrapped `mvn` with `Push-Location $ProjectRoot` / `Pop-Location` (CWD fix when invoked from `monitoring/`).
- Added `Start-Sleep -Seconds 2` after stopping monitor (lets background job release `stats.csv` before Python opens it).

**Em dash encoding fix** in three `.ps1` files: replaced `—` inside double-quoted strings with `--` (PowerShell 5.1 misreads UTF-8 em-dash bytes as string terminators on Windows-1252 codepages).

**`redeploy-local.ps1`**: removed final `Read-Host -Prompt "Press Enter to exit"` (blocked non-interactive callers on this branch).

**WebSPA Angular 21 code smell fixes** (`code_smeells.md` backlog resolved):
- All injectable constructor parameters marked `private readonly` across all service and component files.
- Unused imports removed: `Observer`, `HttpResponse`, `HttpHeaders`, `HttpErrorResponse`, `map`, `HttpTransportType`, `FormBuilder`, `Observable`, `OnInit`, `OnChanges`, `Header`, `Pager`.
- `var` → `const`/`let` in `guid.ts`; `| 0` → `Math.trunc()`.
- `Math.min()` in `pager.ts`; `Math.max()` in `basket.component.ts` to replace ternary expressions.
- Empty lifecycle hooks removed: `ngOnInit()` from `orders-new.component.ts`, `pager.ts`, `page-not-found.component.ts`.
- `window.` → `globalThis.` in `security.service.ts` and `identity.ts`; `typeof x !== 'undefined'` → `x !== undefined`.
- Commented-out code removed from `security.service.ts`, `app.component.ts`, `polyfills.ts`.
- Negated conditions inverted in `security.service.ts` (`AuthorizedCallback`).
- `throw 'string'` → `throw new Error('string')` in `security.service.ts`.
- `subscribe(next, error, complete)` → observer-object form in `security.service.ts` and `basket.component.ts`.
- Duplicate `Router`/`ActivatedRoute` import merged in `security.service.ts`.
- `String` wrapper type → primitive `string` in `basket.component.ts`.
- `deleteItem(id: String)` → `deleteItem(id: string)`.
- All `Subject` streams that are written-once → `readonly`; `settingsLoaded$` from `Subject` → `ReplaySubject(1)` (config race-condition fix).
- Redundant `return;` statements removed (storage.service.ts, orders.service.ts).
- Duplicate CSS `&-text` selector merged in `_utilities.scss`.
- Invalid CSS `:selected` pseudo-class replaced with `:checked` in `catalog.component.scss`.
- All `<img>` elements across HTML templates given meaningful `alt` attributes.
- `lang="en"` added to `<html>` in `index.html`.
- Form `<label>` elements in `orders-new.component.html` associated with `for`/`id` pairs.
- Clickable `<div>`/`<span>` elements converted to semantic `<button>` elements: catalog item, basket delete, pager Previous/Next, identity login/logout.
- Redundant `true` boolean literals removed from `[ngClass]` in `app.component.html`, `catalog.component.html`, `basket.component.html`.
- CSS button reset added to `.esh-catalog-item` for the `<button>` element conversion.
- Docker `FROM ... as` → `FROM ... AS` in 3 Dockerfiles (S6476).
- Deprecated global `event.preventDefault()` removed from `CatalogComponent.onPageChanged()`.
- **Intentionally NOT migrated**: `standalone: false` (kept — NgModule architecture for all child components/modules).
- **Deprecated API migrations completed**:
  - `BrowserAnimationsModule` → `provideAnimations()` (provider in `main.ts`).
  - `HttpClientModule` / `HttpClientJsonpModule` removed from `SharedModule`; `provideHttpClient()` added to `main.ts`.
  - `platformBrowserDynamic` → `bootstrapApplication`: `AppComponent` converted to `standalone: true` (imports `SharedModule`, `BasketModule`); `app.module.ts` deleted; all providers moved to `main.ts`.
  - `BrowserDynamicTestingModule` / `platformBrowserDynamicTesting` → `BrowserTestingModule` / `platformBrowserTesting` from `@angular/platform-browser/testing` in `test.ts`.

### Branch `ft-monitoring-resource-consuption`

- Added `monitoring/` folder with local Docker stats monitoring tool.
- `BaseLoggedClass.clearUserBasket()`: added HTTP status logging, JWT part-count guard, `sub` claim guard, `mod==1` Base64 padding branch, narrowed catch to `IOException | JsonParseException`.
- `BasketWebSPA`: extracted `PAGER_INFO_LOCATOR` constant and `waitForPagerUpdate()` helper; refactored `selectFilter` and `numberCatalogDisplayedItems` to use them.
- `WebSPACatalogTests.testCatalogPaginationSPA`: uses `waitForPagerUpdate()` via typed cast.
- `WebMVCCatalogTests.testCatalogPagination`: replaced `numberOfElementsToBeMoreThan` post-click waits with `stalenessOf()`.
- Spacing, blank-line, and stale-Javadoc fixes across several test files.

