#!/bin/bash
set -e
export PATH="$PATH:/root/.dotnet/tools"
echo "==> Installing dotnet-sonarscanner..."
dotnet tool install --global dotnet-sonarscanner --verbosity quiet 2>/dev/null || true
echo "==> SonarScanner begin..."
dotnet sonarscanner begin \
  /k:"eshop-local" \
  /n:"eShopContainers (local)" \
  /d:sonar.host.url="http://host.docker.internal:9000" \
  /d:sonar.token="${SONAR_LOCAL_TOKEN}" \
  /d:sonar.projectBaseDir="/repo/sut" \
  /d:sonar.exclusions="build/**,deploy/**,**/node_modules/**,**/bin/**,**/obj/**,**/*Migrations/**,**/wwwroot/lib/**,**/*.min.css,**/*.min.js,**/.angular/**,**/*.png,**/*.jpg,**/*.ico,**/*.zip,**/*.pfx,**/*.woff,**/*.woff2,**/*.ttf,**/*.eot,**/Dockerfile.develop,**/Identity.API/wwwroot/css/**" \
  /d:sonar.cpd.exclusions="**/FunctionalTests/**,**/ApiGateways/Web.Bff.Shopping/**,**/ApiGateways/Mobile.Bff.Shopping/**"
echo "==> Restoring packages..."
dotnet restore sut/src/eShopOnContainers-ServicesAndWebApps.sln --verbosity quiet
echo "==> Building..."
dotnet build sut/src/eShopOnContainers-ServicesAndWebApps.sln --no-restore --verbosity quiet /maxcpucount:1
echo "==> SonarScanner end..."
dotnet sonarscanner end /d:sonar.token="${SONAR_LOCAL_TOKEN}"
echo "==> Done."
