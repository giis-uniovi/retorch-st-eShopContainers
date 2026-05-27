<#
.SYNOPSIS
    Stops the background monitoring job started by start-monitoring.ps1.

.EXAMPLE
    .\monitoring\stop-monitoring.ps1
#>

$DataDir   = Join-Path $PSScriptRoot "data"
$CsvFile   = Join-Path $DataDir "stats.csv"
$JobIdFile = Join-Path $DataDir "monitor.jobid"

if (-not (Test-Path $JobIdFile)) {
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [WARN] No job ID file found at $JobIdFile -- is monitoring running?"
    exit 0
}

$jobId = (Get-Content $JobIdFile -Raw).Trim()
$job   = Get-Job -Id $jobId -ErrorAction SilentlyContinue

if ($null -ne $job) {
    Stop-Job   -Id $jobId
    Remove-Job -Id $jobId
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [INFO] Monitor stopped (Job ID=$jobId)"
} else {
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [WARN] Job $jobId not found -- already stopped?"
}

Remove-Item $JobIdFile -Force -ErrorAction SilentlyContinue

if (Test-Path $CsvFile) {
    # Count data rows, excluding the header line
    $samples = (Get-Content $CsvFile).Count - 1
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [INFO] Collected $samples measurements."
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [INFO] Generate the report with:"
    Write-Host "       python monitoring\generate-excel.py"
}
