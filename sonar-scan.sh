#!/bin/bash
# Runs inside the mcr.microsoft.com/dotnet/sdk:10.0 Docker container.
# Called by run-sonar-local.ps1 — do not invoke directly.
set -e

export PATH="$PATH:/root/.dotnet/tools"

echo "==> Installing dotnet-sonarscanner..."
dotnet tool install --global dotnet-sonarscanner --verbosity quiet

echo "==> Parsing sonar.properties..."
PROPS=()
while IFS= read -r line || [[ -n "$line" ]]; do
  line="${line%$'\r'}"
  [[ "$line" =~ ^[[:space:]]*# ]] && continue
  [[ -z "${line// }" ]] && continue
  PROPS+=("/d:$line")
done < sonar.properties

echo "==> SonarScanner begin (branch: ${SONAR_BRANCH})..."
dotnet sonarscanner begin \
  /k:"my:retorch-st-eShopContainers" \
  /n:"retorch-st-eShopContainers" \
  /o:"giis" \
  /d:sonar.projectBaseDir="/repo/sut" \
  /d:sonar.branch.name="${SONAR_BRANCH}" \
  "${PROPS[@]}"

echo "==> Restoring NuGet packages..."
dotnet restore sut/src/eShopOnContainers-ServicesAndWebApps.sln

echo "==> Building SUT..."
dotnet build sut/src/eShopOnContainers-ServicesAndWebApps.sln --no-restore

echo "==> SonarScanner end (uploading to SonarCloud)..."
dotnet sonarscanner end
