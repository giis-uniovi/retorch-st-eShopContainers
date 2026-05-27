<#
.SYNOPSIS
    Full local test-suite runner with resource monitoring.

.DESCRIPTION
    Runs the following steps in order:

      0. Create monitoring\.venv and install openpyxl (skipped if venv already exists).
      1. Deploy the SUT via redeploy-local.ps1 (full build by default).
      2. Start docker stats monitoring (background job, configurable interval).
      3. Run mvn test with TJOB_NAME=local and SUT_URL=http://localhost:5100.
      4. Stop monitoring.
      5. Generate the Excel resource report.

    The script exits with the same exit code as mvn so CI / scripts can detect
    test failures. The Excel report is always generated even when tests fail.

.PARAMETER NoBuild
    Pass through to redeploy-local.ps1 to skip the full image rebuild.
    The Envoy gateway images are always rebuilt regardless of this flag.

.PARAMETER Interval
    docker stats sampling interval in seconds. Default: 5.

.PARAMETER Test
    Optional Maven -Dtest filter.
    Examples: "WebSPACatalogTests"
              "WebSPACatalogTests#testCatalogPaginationSPA"
              "WebSPACatalogTests+WebSPABasketTests"
    Omit to run the entire test suite.

.EXAMPLE
    # Full run — build images, run all tests, generate report
    .\monitoring\run-local-suite.ps1

    # Skip rebuild (SUT already up to date), run only catalog tests
    .\monitoring\run-local-suite.ps1 -NoBuild -Test "WebSPACatalogTests"

    # Custom sampling interval
    .\monitoring\run-local-suite.ps1 -NoBuild -Interval 10
#>
param(
    [switch]$NoBuild,
    [int]   $Interval = 5,
    [string]$Test = ''
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$ProjectRoot   = Split-Path -Parent $PSScriptRoot
$MonitoringDir = $PSScriptRoot
$VenvDir       = Join-Path $MonitoringDir '.venv'
$VenvPython    = Join-Path $VenvDir 'Scripts\python.exe'
$ReqFile       = Join-Path $MonitoringDir 'requirements.txt'

# ── Logging helper ─────────────────────────────────────────────────────────────
function Log {
    param([string]$Level, [string]$Msg)
    $color = switch ($Level) {
        'OK'    { 'Green'  }
        'WARN'  { 'Yellow' }
        'ERROR' { 'Red'    }
        default { 'Cyan'   }
    }
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [$Level] $Msg" -ForegroundColor $color
}

# ── Step 0 — Python venv ───────────────────────────────────────────────────────
Log 'INFO' '=== Step 0: Python virtual environment ==='

if (-not (Test-Path $VenvPython)) {
    Log 'INFO' "Creating venv at $VenvDir ..."

    # Verify Python 3 is available
    $null = & python --version 2>&1
    if ($LASTEXITCODE -ne 0) {
        Log 'ERROR' 'Python 3 is not in PATH. Install it from https://python.org and retry.'
        exit 1
    }

    & python -m venv $VenvDir
    if ($LASTEXITCODE -ne 0) { Log 'ERROR' 'python -m venv failed.'; exit 1 }

    Log 'INFO' 'Installing dependencies ...'
    & $VenvPython -m pip install --quiet --upgrade pip
    & $VenvPython -m pip install --quiet -r $ReqFile
    if ($LASTEXITCODE -ne 0) { Log 'ERROR' 'pip install failed.'; exit 1 }

    Log 'OK' 'Virtual environment ready.'
} else {
    Log 'INFO' 'Virtual environment already exists — skipping setup.'
}

# ── Step 1 — Deploy SUT ────────────────────────────────────────────────────────
Log 'INFO' '=== Step 1: Deploy SUT ==='

$redeployScript = Join-Path $ProjectRoot 'redeploy-local.ps1'
$redeployArgs   = if ($NoBuild) { @('-NoBuild') } else { @() }

$ErrorActionPreference = 'Continue'
& $redeployScript @redeployArgs
$deployExit = $LASTEXITCODE
$ErrorActionPreference = 'Stop'

if ($deployExit -ne 0) {
    Log 'ERROR' "SUT deployment failed (exit=$deployExit). Aborting."
    exit $deployExit
}
Log 'OK' 'SUT is up and healthy.'

# ── Step 2 — Start monitoring ──────────────────────────────────────────────────
Log 'INFO' '=== Step 2: Start resource monitoring ==='

$ErrorActionPreference = 'Continue'
& "$MonitoringDir\start-monitoring.ps1" -Interval $Interval
$monitorStarted = ($LASTEXITCODE -eq 0)
$ErrorActionPreference = 'Stop'

if (-not $monitorStarted) {
    Log 'WARN' 'Monitoring could not start — test run will continue without it.'
}

# ── Step 3 — Run test suite ────────────────────────────────────────────────────
Log 'INFO' '=== Step 3: Run test suite ==='

$mvnArgs = @('test', '-DTJOB_NAME=local')
if ($Test -ne '') {
    $mvnArgs += "-Dtest=$Test"
    Log 'INFO' "Test filter: $Test"
} else {
    Log 'INFO' 'Running full test suite (no -Dtest filter).'
}

Log 'INFO' "mvn $($mvnArgs -join ' ')"
$ErrorActionPreference = 'Continue'
Push-Location $ProjectRoot
& mvn @mvnArgs
$mvnExit = $LASTEXITCODE
Pop-Location
$ErrorActionPreference = 'Stop'

Log 'INFO' "mvn exited with code $mvnExit."

# ── Step 4 — Stop monitoring ───────────────────────────────────────────────────
Log 'INFO' '=== Step 4: Stop monitoring ==='

if ($monitorStarted) {
    $ErrorActionPreference = 'Continue'
    & "$MonitoringDir\stop-monitoring.ps1"
    $ErrorActionPreference = 'Stop'
    # Allow the background job's file handles to be fully released before Python opens stats.csv
    Start-Sleep -Seconds 2
}

# ── Step 5 — Generate Excel report ────────────────────────────────────────────
Log 'INFO' '=== Step 5: Generate Excel report ==='

$report = Join-Path $MonitoringDir 'data\resource-report.xlsx'

$ErrorActionPreference = 'Continue'
& $VenvPython "$MonitoringDir\generate-excel.py" `
    --data-dir "$MonitoringDir\data" `
    --output   $report
$xlsxExit = $LASTEXITCODE
$ErrorActionPreference = 'Stop'

if ($xlsxExit -eq 0) {
    Log 'OK' "Report written -> $report"
} else {
    Log 'WARN' 'Excel generation failed. Check that docker stats data was collected.'
}

# ── Summary ────────────────────────────────────────────────────────────────────
Write-Host ''
if ($mvnExit -eq 0) {
    Log 'OK' 'All tests passed.'
} else {
    Log 'WARN' "Tests finished with failures (mvn exit=$mvnExit) -- see target\surefire-reports."
}

exit $mvnExit
