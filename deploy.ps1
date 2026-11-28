# deploy.ps1 - Deploy (or tear down) the eShopContainers SUT locally. Run from the project root directory.
#
# Usage:
#   .\deploy.ps1             # tear down any previous deployment, build the images and deploy
#   .\deploy.ps1 -NoBuild    # skip the image build, just redeploy
#   .\deploy.ps1 -Down       # tear down the local deployment

param(
    [switch]$NoBuild,
    [switch]$Down
)

$ErrorActionPreference = "Stop"

$ScriptDir   = Split-Path -Parent $MyInvocation.MyCommand.Path
$SutSrc      = Join-Path $ScriptDir "sut\src"
$EnvFile     = Join-Path $ScriptDir ".retorch\envfiles\local.env"
$ComposeFile = Join-Path $SutSrc "docker-compose.yml"
$OverrideFile= Join-Path $SutSrc "docker-compose.local-override.yml"
$ProjectName = "local"

# On Windows with Docker Desktop, host.docker.internal is automatically available
# in both containers and the Windows hosts file. No detection needed.
$env:HOST_IP = "host.docker.internal"

function Invoke-Compose {
    param([string[]]$CommandArgs)
    $baseArgs = @(
        "compose",
        "-f", $ComposeFile,
        "-f", $OverrideFile,
        "--env-file", $EnvFile,
        "-p", $ProjectName
    )
    # docker writes warnings/progress to stderr; with ErrorActionPreference=Stop PowerShell 5.1
    # would turn those into terminating errors, so rely on the exit code only.
    $prevPref = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        & docker @baseArgs @CommandArgs 2>&1 | ForEach-Object { "$_" }
    } finally {
        $ErrorActionPreference = $prevPref
    }
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose failed with exit code $LASTEXITCODE"
    }
}

if ($Down) {
    Write-Host "=== Tearing down the local SUT deployment ===" -ForegroundColor Cyan
    Invoke-Compose @("down", "--volumes", "--remove-orphans")
    exit 0
}

Write-Host "=== [1/4] Tearing down existing SUT deployment ===" -ForegroundColor Cyan
try {
    Invoke-Compose @("down", "--volumes", "--remove-orphans")
} catch {
    Write-Host "  (no existing deployment or teardown warning - continuing)" -ForegroundColor Yellow
}

if (-not $NoBuild) {
    Write-Host "=== [2/4] Building Envoy gateway images (require TAG substitution) ===" -ForegroundColor Cyan
    Invoke-Compose @("build", "webshoppingapigw", "mobileshoppingapigw")

    Write-Host "=== [3/4] Building remaining SUT images ===" -ForegroundColor Cyan
    Invoke-Compose @("build")
} else {
    Write-Host "=== [2/4] Skipping full build (-NoBuild flag set) ===" -ForegroundColor Yellow
    Write-Host "=== [3/4] Building Envoy gateway images (always rebuilt for correct TAG) ===" -ForegroundColor Cyan
    Invoke-Compose @("build", "webshoppingapigw", "mobileshoppingapigw")
}

Write-Host "=== [4/4] Starting SUT containers ===" -ForegroundColor Cyan
Invoke-Compose @("up", "-d")

Write-Host ""
Write-Host "Waiting for WebMVC to be ready (up to 200 seconds)..." -ForegroundColor Cyan
$Counter   = 0
$WaitLimit = 40
$Ready     = $false

while ($Counter -lt $WaitLimit) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:5100" `
            -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.Content -match "esh-catalog-item") {
            $Ready = $true
            break
        }
    } catch {
        # Not ready yet
    }
    $Counter++
    Write-Host ("  attempt {0}/{1}" -f $Counter, $WaitLimit) -NoNewline
    Write-Host "`r" -NoNewline
    Start-Sleep -Seconds 5
}

Write-Host ""

if (-not $Ready) {
    Write-Host "ERROR: SUT did not become ready in time. Showing last container logs:" -ForegroundColor Red
    try { Invoke-Compose @("logs", "--tail", "30") } catch {}
    exit 1
}

Write-Host "============================================" -ForegroundColor Green
Write-Host " SUT is ready!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host "  WebMVC      http://localhost:5100"
Write-Host "  WebSPA      http://localhost:5104"
Write-Host "  WebStatus   http://localhost:5107"
Write-Host "  Identity    http://localhost:5105"
Write-Host "  Seq (logs)  http://localhost:5340"
Write-Host "  RabbitMQ    http://localhost:15672  (guest/guest)"
Write-Host "  SQL Server  localhost:5433         (sa/Pass@word)"
Write-Host ""
Write-Host "To tear down:  .\deploy.ps1 -Down"

Write-Host "Process complete."
if ([Environment]::UserInteractive -and -not [Console]::IsInputRedirected) {
    Read-Host -Prompt "Press Enter to exit"
}