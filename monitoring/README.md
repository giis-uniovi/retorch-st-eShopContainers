# Resource Monitoring — eShopContainers local test suite

Standalone tool that measures CPU and memory consumption of every running Docker container
(SUT microservices + Selenoid browser nodes) while the test suite executes locally,
and exports the results to a formatted Excel workbook.

> **Scope:** local sequential runs only. Not wired into Jenkins or the `.retorch` lifecycle scripts.

---

## Prerequisites

| Requirement | Notes |
|---|---|
| Docker Desktop | Running, with the Docker CLI available in PATH |
| Java 11 + Maven | Required to run the test suite (`mvn test`) |
| Python 3.8+ | For the Excel generator. `python` must be in PATH |
| PowerShell 5.1+ | Comes with Windows 10/11. For the `.ps1` scripts |

---

## Quick start (recommended)

Open a **PowerShell** terminal at the project root and run:

```powershell
.\monitoring\run-local-suite.ps1
```

That single command:

1. Creates a Python virtual environment (`monitoring\.venv`) and installs `openpyxl` — **only on the first run**.
2. Tears down any existing SUT containers, builds images, and starts fresh via `redeploy-local.ps1`.
3. Starts sampling `docker stats` every **5 seconds** in the background.
4. Runs the full Maven test suite (`mvn test -DTJOB_NAME=local -DSUT_URL=http://localhost:5100`).
5. Stops the sampler.
6. Writes `monitoring\data\resource-report.xlsx`.

The script exits with Maven's exit code, so test failures are visible to the caller.

---

## run-local-suite.ps1 — parameters

```
.\monitoring\run-local-suite.ps1 [-NoBuild] [-Interval <seconds>] [-Test <filter>]
```

| Parameter | Type | Default | Description |
|---|---|---|---|
| `-NoBuild` | switch | off | Skip the full Docker image rebuild. The Envoy gateway images are always rebuilt regardless of this flag. Use when SUT source has not changed. |
| `-Interval` | int | `5` | docker stats sampling interval in seconds. Lower = more data points, higher CPU overhead. |
| `-Test` | string | *(all)* | Maven `-Dtest` filter. See examples below. |

### Examples

```powershell
# First run — full build, all tests, 5 s interval
.\monitoring\run-local-suite.ps1

# Already deployed — skip rebuild, run only catalog tests
.\monitoring\run-local-suite.ps1 -NoBuild -Test "WebSPACatalogTests"

# Single test method, 10 s interval
.\monitoring\run-local-suite.ps1 -NoBuild -Interval 10 -Test "WebSPACatalogTests#testCatalogPaginationSPA"

# Two test classes at once
.\monitoring\run-local-suite.ps1 -NoBuild -Test "WebSPABasketTests+WebSPACatalogTests"

# API tests only
.\monitoring\run-local-suite.ps1 -NoBuild -Test "BasketAPITests,CatalogAPITests"
```

---

## Manual workflow

Use the individual scripts when you need finer control — for example, to keep the
SUT running between test runs without redeploying each time.

### PowerShell

```powershell
# 1. Deploy (once)
.\redeploy-local.ps1

# 2. Start monitoring
.\monitoring\start-monitoring.ps1              # default 5 s interval
.\monitoring\start-monitoring.ps1 -Interval 10 # custom interval

# 3. Run tests (repeat as needed)
mvn test -DTJOB_NAME=local -DSUT_URL=http://localhost:5100

# 4. Stop monitoring
.\monitoring\stop-monitoring.ps1

# 5. Generate report
python monitoring\generate-excel.py
# or use the venv python:
monitoring\.venv\Scripts\python.exe monitoring\generate-excel.py
```

### Bash (Git Bash / WSL)

```bash
# Deploy
./redeploy-local.sh

# Start monitoring
bash monitoring/start-monitoring.sh            # default 5 s
bash monitoring/start-monitoring.sh 10         # custom interval

# Run tests
mvn test -DTJOB_NAME=local -DSUT_URL=http://localhost:5100

# Stop monitoring
bash monitoring/stop-monitoring.sh

# Generate report
python3 monitoring/generate-excel.py
```

---

## Excel report

Output: `monitoring\data\resource-report.xlsx`

| Sheet | Content |
|---|---|
| **Summary** | One row per container. Avg / Max / Min / P95 CPU (%) and Memory (MiB), average Memory (%), sample count. |
| **SUT Containers** | Same metrics, filtered to microservice containers only (non-browser). |
| **Browsers** | `chrome-node`, `chrome-video`, `selenium-hub` — isolated so browser overhead is visible separately. |
| **Raw Data** | Every measurement row. Auto-filter enabled so you can sort/filter by container, timestamp, or metric. |

Browser containers are identified automatically by matching `chrome-node`, `chrome-video`, `selenoid`, or `selenium-hub` in the container name.

### Column reference (Summary / SUT / Browsers sheets)

| Column | Meaning |
|---|---|
| Container | Docker container name (e.g. `webmvc_local`) |
| Avg CPU (%) | Mean CPU usage across all samples |
| Max CPU (%) | Peak CPU usage observed |
| Min CPU (%) | Lowest CPU usage observed |
| P95 CPU (%) | 95th-percentile CPU — excludes brief spikes |
| Avg Mem (MiB) | Mean resident memory in MiB |
| Max Mem (MiB) | Peak memory observed |
| Min Mem (MiB) | Lowest memory observed |
| P95 Mem (MiB) | 95th-percentile memory |
| Avg Mem (%) | Mean memory as % of container limit |
| Samples | Number of data points collected |

---

## How it works

```
docker stats --no-stream         (every N seconds, background job)
        │
        ▼
monitoring\data\stats.csv        (one CSV row per container per tick)
        │
        ▼
generate-excel.py                (parses CSV, aggregates, writes .xlsx)
        │
        ▼
monitoring\data\resource-report.xlsx
```

`start-monitoring.ps1` launches a PowerShell background **Job** (`Start-Job`) that loops
indefinitely, calling `docker stats --no-stream` against all running containers and appending
the results to `monitoring\data\stats.csv`.  The Job ID is saved to `monitoring\data\monitor.jobid`
so `stop-monitoring.ps1` can find and kill it by ID.

The bash scripts use the same logic with a background subshell (`&`) and a `.pid` file.

---

## File reference

```
monitoring/
├── run-local-suite.ps1       ← All-in-one: venv + deploy + monitor + test + report
│
├── start-monitoring.ps1      ← Start background docker stats sampler (PowerShell)
├── stop-monitoring.ps1       ← Stop sampler, print sample count (PowerShell)
├── run-with-monitoring.ps1   ← Wrap any command with start/stop/report (PowerShell)
│
├── start-monitoring.sh       ← Same as above (Bash)
├── stop-monitoring.sh        ← Same as above (Bash)
├── run-with-monitoring.sh    ← Same as above (Bash)
│
├── generate-excel.py         ← Reads stats.csv, writes resource-report.xlsx
├── requirements.txt          ← openpyxl>=3.1.0
│
├── .venv/                    ← Python virtual environment (auto-created, git-ignored)
└── data/                     ← Runtime data (git-ignored)
    ├── stats.csv             ← Raw docker stats measurements
    ├── monitor.jobid         ← Background job/PID (present while sampler is running)
    └── resource-report.xlsx  ← Excel output
```

---

## Troubleshooting

**`python` is not recognised**  
Install Python 3 from [python.org](https://www.python.org/downloads/) and make sure
"Add python.exe to PATH" is checked during installation.

**`mvn` is not recognised**  
Add Maven's `bin/` directory to your `PATH` environment variable, or run from an
IDE terminal that already has it configured.

**`monitor already running` warning**  
A previous run did not stop cleanly. Run `.\monitoring\stop-monitoring.ps1` to clear the stale job,
then try again.

**Excel generated but all values are zero**  
The sampler started after the containers had already exited, or `docker stats` could not
reach the Docker daemon. Verify Docker Desktop is running and that containers are visible
with `docker ps` before starting the suite.

**Execution policy error on `.ps1` scripts**  
Run the following once in an elevated PowerShell:
```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```
