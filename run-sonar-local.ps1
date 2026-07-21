param(
    [switch]$Local,
    [string]$Branch = (git branch --show-current)
)

$repoRoot = (git rev-parse --show-toplevel) -replace '\\', '/'

if ($Local) {
    # Target the SonarQube instance running locally on Docker (port 9000)
    $localToken = $env:SONAR_LOCAL_TOKEN
    if (-not $localToken) {
        Write-Error "SONAR_LOCAL_TOKEN is not set.`nRun: `$env:SONAR_LOCAL_TOKEN = 'squ_...'"
        exit 1
    }
    Write-Host "Branch  : $Branch"
    Write-Host "Target  : http://localhost:9000 (local SonarQube)"
    Write-Host "Repo    : $repoRoot"
    Write-Host ""
    Write-Host "Pulling mcr.microsoft.com/dotnet/sdk:10.0 (first run: ~700 MB)..."

    docker run --rm `
        -v "${repoRoot}:/repo" `
        -w /repo `
        --add-host "host.docker.internal:host-gateway" `
        -e SONAR_LOCAL_TOKEN=$localToken `
        mcr.microsoft.com/dotnet/sdk:10.0 `
        bash sonar-local.sh

    Write-Host ""
    Write-Host "Scan complete. Results: http://localhost:9000/project/issues?id=eshop-local"
} else {
    if (-not $env:SONAR_TOKEN) {
        Write-Error "SONAR_TOKEN is not set.`nRun: `$env:SONAR_TOKEN = 'sqp_...'"
        exit 1
    }
    Write-Host "Branch  : $Branch"
    Write-Host "Target  : https://sonarcloud.io (SonarCloud)"
    Write-Host "Repo    : $repoRoot"
    Write-Host ""
    Write-Host "Pulling mcr.microsoft.com/dotnet/sdk:10.0 (first run: ~700 MB)..."

    docker run --rm `
        -v "${repoRoot}:/repo" `
        -w /repo `
        -e SONAR_TOKEN=$env:SONAR_TOKEN `
        -e SONAR_BRANCH=$Branch `
        mcr.microsoft.com/dotnet/sdk:10.0 `
        bash sonar-scan.sh

    Write-Host ""
    Write-Host "Scan complete. Results:"
    Write-Host "  https://sonarcloud.io/project/issues?id=my:retorch-st-eShopContainers&branch=$Branch"
}
