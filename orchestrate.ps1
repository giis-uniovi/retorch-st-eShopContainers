# 1. Hardcode your arguments here
$RootPackageNameTests = "giis.eshopcontainers.e2e.functional.tests"
$SystemName           = "EShopOnContainers"
$JenkinsFilePath      = "./"

# 2. Search for the JAR file in the script's home directory
$JarFile = Get-ChildItem -Path $PSScriptRoot -Filter "retorch-orchestration-*-standalone.jar" | Select-Object -First 1

# 3. Check if a matching file was found
if ($null -eq $JarFile) {
    Write-Error "Error: No file matching 'retorch-orchestration-*-standalone.jar' was found in '$PSScriptRoot'."
    exit 1
}

Write-Host "Found JAR at: $($JarFile.FullName)" -ForegroundColor Green
Write-Host "Executing command..." -ForegroundColor Cyan

# 4. Execute from the project root so all relative paths (.retorch/, target/) resolve correctly
Set-Location $PSScriptRoot

# 5. Compile test classes and copy transitive dependencies so the JAR can load annotated test classes
Write-Host "Preparing test classes and dependencies..." -ForegroundColor Cyan
mvn test-compile dependency:copy-dependencies "-DincludeScope=test" -q
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven preparation failed (exit code $LASTEXITCODE)."
    exit 1
}

# 6. Run the orchestration generator
java -jar $JarFile.FullName $RootPackageNameTests $SystemName $JenkinsFilePath

# Example of a command java -jar retorch-orchestration-1.2.1-60-creation-of-a-tool-cli-with-retorch-orchestration-20260527.130705-3-standalone.jar "giis.eshopcontainers.e2e.functional.tests" "EShopOnContainers" "./"