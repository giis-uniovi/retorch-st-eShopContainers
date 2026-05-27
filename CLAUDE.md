# CLAUDE.md — eShopContainers E2E Test Suite

> **Maintenance rule:** Every time a file in this repository is created, modified, or deleted in a conversation with Claude, update this file to reflect the change before closing the task. Keep the sections below accurate and concise.

---

## Project overview

End-to-end test suite for the [eShopOnContainers](https://github.com/dotnet-architecture/eShopOnContainers) microservices reference application.
Tests cover the WebMVC and WebSPA frontends plus the REST APIs (Basket, Catalog, Ordering, Identity, Payment).
The project integrates with **Retorch** for parallel test orchestration and uses **Selenoid** for browser automation.

---

## Technology stack

| Layer | Technology |
|---|---|
| Test framework | JUnit 5 (Jupiter) + Selenium 4 |
| Build | Maven 3 (Surefire 3.5.5) |
| Browser orchestration | Selenoid (`chrome-node`, `chrome-video`, `selenium-hub`) |
| Selema integration | `giis-uniovi/selema` (session/grid lifecycle) |
| SUT | Docker Compose — 24 microservice containers |
| CI / orchestration | Jenkins + Retorch (parallel TJob model) |
| Code quality | SonarLint, SpotBugs |

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
Jenkinsfile         16 parallel TJobs across 3 stages
```

---

## Container / TJob naming

- In CI every Docker service is named `{service}_{tjobName}` (e.g. `webmvc_tjoba`).
- The `TJOB_NAME` env var is set by the `.retorch/envfiles/{tjob}.env` file and passed to Docker Compose via `${TJOB_NAME:-default}`.
- Locally (without setting `TJOB_NAME`) containers are named `{service}_default`.
- 16 TJobs total: `tjoba` … `tjobp`, spread across Stage 0 (a–f), Stage 1 (g–k), Stage 2 (l–p).

---

## Running tests locally (sequential)

```bash
# 1. Start SUT
cd sut/src
docker compose -f docker-compose.yml --env-file ../../.retorch/envfiles/tjoba.env up -d
cd ../..

# 2. Run tests (all, or a subset)
mvn test
mvn test -Dtest=WebSPACatalogTests

# 3. Tear down
cd sut/src
docker compose -f docker-compose.yml --env-file ../../.retorch/envfiles/tjoba.env down --volumes
```

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

### Manual one-shot wrapper

```powershell
# Install dependency once
pip install -r monitoring\requirements.txt

# Run the whole test suite with monitoring (PowerShell — Windows)
.\monitoring\run-with-monitoring.ps1 -Command "mvn test"

# Custom sampling interval (10 s instead of the default 5 s)
.\monitoring\run-with-monitoring.ps1 -Interval 10 -Command "mvn test -Dtest=WebSPACatalogTests"
```

```bash
# Bash equivalent (Git Bash / WSL / Linux)
bash monitoring/run-with-monitoring.sh "mvn test"
bash monitoring/run-with-monitoring.sh 10 "mvn test -Dtest=WebSPACatalogTests"
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

## Key classes

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

---

## Recent significant changes (branch `ft-monitoring-resource-consuption`)

- Added `monitoring/` folder with local Docker stats monitoring tool.
- `BaseLoggedClass.clearUserBasket()`: added HTTP status logging, JWT part-count guard, `sub` claim guard, `mod==1` Base64 padding branch, narrowed catch to `IOException | JsonParseException`.
- `BasketWebSPA`: extracted `PAGER_INFO_LOCATOR` constant and `waitForPagerUpdate()` helper; refactored `selectFilter` and `numberCatalogDisplayedItems` to use them.
- `WebSPACatalogTests.testCatalogPaginationSPA`: uses `waitForPagerUpdate()` via typed cast.
- `WebMVCCatalogTests.testCatalogPagination`: replaced `numberOfElementsToBeMoreThan` post-click waits with `stalenessOf()`.
- Spacing, blank-line, and stale-Javadoc fixes across several test files.
