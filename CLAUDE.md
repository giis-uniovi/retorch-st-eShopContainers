# CLAUDE.md — retorch-st-eShopContainers

Demonstrator of the [RETORCH](https://github.com/giis-uniovi/retorch) framework: a Java end-to-end suite (Selenium UI + HTTP API tests)
for [eShopOnContainers](https://github.com/dotnet-architecture/eShopOnContainers), whose sources are in `sut/`. See
[`sut/CLAUDE.md`](sut/CLAUDE.md) for the SUT (stack, services, SonarCloud, version gotchas) and `README.md` for the user documentation.

## RETORCH rules (do not break)

- Every test method carries `@AccessMode(...)` annotations (`giis.retorch.annotations`) that declare the logical resources it uses and
  how (`READONLY`/`READWRITE`, concurrency, sharing). The TJob schedule of the `Jenkinsfile` is **generated from them**.
  - Never drop or alter existing `@AccessMode` annotations when refactoring.
  - Never rename test classes or methods: the `Jenkinsfile` references them as `Class#method`.
  - A new test method needs sensible `@AccessMode` values (copy a sibling) and is only run by CI after regenerating the pipeline.
- `Jenkinsfile`, `.retorch/scripts/`, `.retorch/envfiles/` (except `local.env`), `.retorch/executionplan.json` and the resource model in
  `.retorch/configurations/*SystemResources.json` are **generated** by `orchestrate.ps1` (it needs the
  `retorch-orchestration-*-standalone.jar` of the RETORCH tool in the repository root). Do not edit them by hand; put SUT-specific
  commands in `.retorch/customscriptscode/` and settings in `.retorch/configurations/retorchCI.properties`.
- Only `retorch-annotations` is a dependency of the tests; the orchestration code is never part of this project.
- `profiling=true` in `retorchCI.properties` makes the pipeline measure the resources of every TJob and publish
  `artifacts/usageprofile/usageprofile.html`. `.retorch/customscriptscode/custom-usageprofile` temporarily builds the profiling tool from
  source; delete it when a release publishes the jar.

## Layout

| Path | What it is |
|---|---|
| `src/test/java/giis/eshopcontainers/e2e/functional/` | The suite: `tests/webmvc`, `tests/webspa`, `tests/api`, `common/` (base classes), `model/`, `utils/` |
| `src/test/resources/test.properties` | Local URLs (`LOCALHOST_*`), test user, API client settings |
| `sut/` | The SUT (`sut/src/docker-compose.yml` + sources) |
| `deploy.sh` / `deploy.ps1` | Local deployment (`--no-build`/`-NoBuild`, `--down`/`-Down`); project `local`, `.retorch/envfiles/local.env` |
| `.retorch/` | Generated pipeline files + `customscriptscode/`, `configurations/retorchCI.properties` |
| `orchestrate.ps1` + `Jenkinsfile` | Regenerate / the generated pipeline: 16 TJobs (`tjoba`…`tjobp`) in 3 stages |
| `sonar.properties` | SonarCloud settings (not `sonar-project.properties`, which the .NET scanner rejects) |

RETORCH resources: `webmvc`, `webspa`, `identity-api`, `catalog-api`, `basket-api`, `ordering-api`, `payment-api`, `chrome-browser`, `eshopUser`.

## Build and test

- Java 8 source level (no `var`, records or `List.of`), JUnit Jupiter, Selenium 4, Selema (owns the WebDriver lifecycle).
- Local run: `./deploy.sh` (or `.\deploy.ps1`), then `mvn test` (or `-Dtest=WebSPACatalogTests`).
- **Do not pass `-DSUT_URL` locally.** Without it the tests use `LOCALHOST_*` from `test.properties`; with it (CI) the API tests use the Docker
  hostnames of the TJob (`identity_api_<tjob>`), which are not reachable from the host.
- `SELENOID_PRESENT` makes the browsers run in the Selenoid / Selenium Grid hub instead of a local Chrome.
- Containers are named `{service}_{tjob}` (`webmvc_tjoba`; locally `{service}_local`).

Key classes: `BaseLoggedClass` (browser lifecycle, login, `clearUserBasket()`), `BaseWebSPALoggedClass`, `BaseAPIClass` (OAuth2 token +
HTTP client), `NavigationWebSPA`, `BasketWebSPA`, `Waiter` / `Click` (explicit waits, safe click with stale-element retry).

## Conventions

- Comments only for a non-obvious *why*; CSS-class locators get a comment pointing to the template element.
- Catch specific checked exceptions, never `catch (Exception e)`; check JWT parts before indexing them.
- `set -e` in the lifecycle shell scripts. Prefer editing existing code over new abstractions.

## CI

- Jenkins, agent label `xretorch-agent`. Every TJob deploys its **own** copy of the SUT (`--env-file .retorch/envfiles/<tjob>.env
  -p <tjob>`) on the external network `jenkins_network`, runs its tests and tears it down; the TJobs of a stage run in parallel.
- The TJob runs `mvn test -Dtest="<Class#method>,..." -DTJOB_NAME=<tjob> -DSUT_URL=<url>`.
- `pom.xml` builds into `target/${TJOB_NAME}` so parallel TJobs never share compiled classes or reports (profile `local-execution` sets
  `TJOB_NAME=local` when the property is absent).
- `.github/workflows/build.yml` analyses `sut/` with SonarCloud (push only) and does not run the tests.

## Keeping this file useful

Record current state and non-obvious gotchas only (history is in `git log`). If the code and this file disagree, tell the user.
