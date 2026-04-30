# Tear down any existing local SUT deployment and redeploy eShopContainers.
# Run from the project root directory.
#
# Usage:
#   .\redeploy-local.ps1             # full build + deploy
#   .\redeploy-local.ps1 -NoBuild    # skip image build, just redeploy

param(
    [switch]$NoBuild
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
    & docker @baseArgs @CommandArgs
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose failed with exit code $LASTEXITCODE"
    }
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
Write-Host "To tear down:"
Write-Host "  docker compose -f sut\src\docker-compose.yml ``"
Write-Host "    -f sut\src\docker-compose.local-override.yml ``"
Write-Host "    --env-file .retorch\envfiles\local.env ``"
Write-Host "    -p local down --volumes"

Write-Host "Process complete."
Read-Host -Prompt "Press Enter to exit"