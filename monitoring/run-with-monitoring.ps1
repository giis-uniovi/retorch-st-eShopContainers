<#
.SYNOPSIS
    Wraps any command with resource monitoring and generates the Excel report automatically.

.DESCRIPTION
    1. Starts the background docker stats sampler (start-monitoring.ps1).
    2. Executes the supplied command (typically mvn test …).
    3. Stops the sampler (stop-monitoring.ps1).
    4. Calls generate-excel.py to produce monitoring\data\resource-report.xlsx.

    The script exits with the same exit code as the wrapped command so it can be
    used inside CI pipelines or npm/make scripts without hiding test failures.

.PARAMETER Command
    The shell command to run, as a single string.

.PARAMETER Interval
    Sampling interval in seconds. Default: 5.

.EXAMPLE
    .\monitoring\run-with-monitoring.ps1 -Command "mvn test"
    .\monitoring\run-with-monitoring.ps1 -Interval 10 -Command "mvn test -Dtest=WebSPACatalogTests"
    .\monitoring\run-with-monitoring.ps1 -Command "mvn test -Dtest=`"BasketAPITests,OrderingAPITests`""
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$Command,

    [int]$Interval = 5
)

$ErrorActionPreference = 'Stop'

function Log([string]$Level, [string]$Msg) {
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [$Level] $Msg"
}

# ── 1. Start monitoring ───────────────────────────────────────────────────────
Log 'INFO' "Starting resource monitor (interval=${Interval}s) ..."
& "$PSScriptRoot\start-monitoring.ps1" -Interval $Interval
if ($LASTEXITCODE -ne 0) {
    Log 'ERROR' 'start-monitoring.ps1 failed — aborting.'
    exit 1
}

# ── 2. Run the command ────────────────────────────────────────────────────────
Log 'INFO' "Executing: $Command"
$cmdExitCode = 0

# Use $ErrorActionPreference = Continue for the command block so a non-zero exit
# from a native executable (e.g. mvn) does not throw a terminating error.
$ErrorActionPreference = 'Continue'
try {
    Invoke-Expression $Command
    $cmdExitCode = $LASTEXITCODE
} catch {
    Log 'ERROR' "Command threw an exception: $_"
    $cmdExitCode = 1
}
$ErrorActionPreference = 'Stop'

Log 'INFO' "Command finished with exit code $cmdExitCode."

# ── 3. Stop monitoring ────────────────────────────────────────────────────────
Log 'INFO' 'Stopping resource monitor ...'
& "$PSScriptRoot\stop-monitoring.ps1"

# ── 4. Generate Excel report ──────────────────────────────────────────────────
$report = Join-Path $PSScriptRoot "data\resource-report.xlsx"
Log 'INFO' 'Generating Excel report ...'
python "$PSScriptRoot\generate-excel.py" `
    --data-dir "$PSScriptRoot\data" `
    --output   $report

if ($LASTEXITCODE -eq 0) {
    Log 'INFO' "Report written -> $report"
} else {
    Log 'WARN' 'Excel generation failed — check that Python and openpyxl are installed.'
    Log 'WARN' 'Install with:  pip install -r monitoring\requirements.txt'
}

exit $cmdExitCode
