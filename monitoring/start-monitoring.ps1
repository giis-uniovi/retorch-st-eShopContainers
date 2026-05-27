<#
.SYNOPSIS
    Starts a background PowerShell job that samples docker stats every INTERVAL seconds.

.DESCRIPTION
    Polls every running Docker container and appends one CSV row per container per tick
    to monitoring\data\stats.csv.  Use stop-monitoring.ps1 to stop it.

.PARAMETER Interval
    Sampling interval in seconds. Default: 5.

.EXAMPLE
    .\monitoring\start-monitoring.ps1
    .\monitoring\start-monitoring.ps1 -Interval 10
#>
param(
    [int]$Interval = 5
)

$DataDir   = Join-Path $PSScriptRoot "data"
$CsvFile   = Join-Path $DataDir "stats.csv"
$JobIdFile = Join-Path $DataDir "monitor.jobid"

New-Item -ItemType Directory -Force -Path $DataDir | Out-Null

# Guard against a duplicate run
if (Test-Path $JobIdFile) {
    $oldId = (Get-Content $JobIdFile -Raw).Trim()
    $existing = Get-Job -Id $oldId -ErrorAction SilentlyContinue
    if ($null -ne $existing -and $existing.State -eq 'Running') {
        Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [WARN] Monitor already running (Job ID=$oldId). Run stop-monitoring.ps1 first."
        exit 1
    }
    Remove-Item $JobIdFile -Force
}

# Write CSV header (overwrite any previous run)
"timestamp,container,cpu_pct,mem_usage,mem_pct,net_io,block_io,pids" |
    Out-File -FilePath $CsvFile -Encoding utf8 -Force

Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [INFO] Starting resource monitor (interval=${Interval}s) ..."
Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [INFO] Output -> $CsvFile"

# ── Background sampling job ──────────────────────────────────────────────────
# The script block receives absolute paths so it works in a clean runspace.
$job = Start-Job -ScriptBlock {
    param([string]$CsvFile, [int]$Interval)

    # Single-quoted so PowerShell does not expand {{ }}
    $fmt = '{{.Name}},{{.CPUPerc}},{{.MemUsage}},{{.MemPerc}},{{.NetIO}},{{.BlockIO}},{{.PIDs}}'

    while ($true) {
        $ts  = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
        try {
            # docker ps -q returns one ID per line; @() forces array even for a single item
            $ids = @(& docker ps -q 2>$null)
            if ($ids.Count -gt 0) {
                $lines = & docker stats --no-stream --format $fmt @ids 2>$null
                foreach ($line in $lines) {
                    if ($line -and $line.Trim() -ne '') {
                        "$ts,$line" | Add-Content -Path $CsvFile -Encoding utf8
                    }
                }
            }
        } catch {
            # Docker may not be reachable yet — skip this tick silently
        }
        Start-Sleep -Seconds $Interval
    }
} -ArgumentList $CsvFile, $Interval

# Persist the job ID so stop-monitoring.ps1 can find it
"$($job.Id)" | Out-File -FilePath $JobIdFile -Encoding utf8 -Force

Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [INFO] Monitor running as Job ID=$($job.Id)"
Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [INFO] Stop with:  .\monitoring\stop-monitoring.ps1"
