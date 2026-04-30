#!/bin/bash
# Tear down any existing local SUT deployment and redeploy eShopContainers.
# Run from the project root directory.
#
# Usage:
#   ./redeploy-local.sh           # full build + deploy
#   ./redeploy-local.sh --no-build  # skip image build, just redeploy

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SUT_SRC="$SCRIPT_DIR/sut/src"
ENV_FILE="$SCRIPT_DIR/.retorch/envfiles/local.env"
COMPOSE_FILE="$SUT_SRC/docker-compose.yml"
OVERRIDE_FILE="$SUT_SRC/docker-compose.local-override.yml"
PROJECT_NAME="local"
SKIP_BUILD=false

for arg in "$@"; do
  [ "$arg" = "--no-build" ] && SKIP_BUILD=true
done

# Compose command prefix shared by all steps
COMPOSE="docker compose \
  -f $COMPOSE_FILE \
  -f $OVERRIDE_FILE \
  --env-file $ENV_FILE \
  -p $PROJECT_NAME"

echo "=== [1/4] Tearing down existing SUT deployment ==="
$COMPOSE down --volumes --remove-orphans 2>/dev/null || true

if [ "$SKIP_BUILD" = false ]; then
  echo "=== [2/4] Building Envoy gateway images (require TAG substitution) ==="
  $COMPOSE build webshoppingapigw mobileshoppingapigw

  echo "=== [3/4] Building remaining SUT images ==="
  $COMPOSE build
else
  echo "=== [2/4] Skipping build (--no-build flag set) ==="
  echo "=== [3/4] Building Envoy gateway images (always rebuilt for correct TAG) ==="
  $COMPOSE build webshoppingapigw mobileshoppingapigw
fi

echo "=== [4/4] Starting SUT containers ==="
$COMPOSE up -d

echo ""
echo "Waiting for WebMVC to be ready (up to 200 seconds)..."
COUNTER=0
WAIT_LIMIT=40

until curl -sf "http://localhost:5100" 2>/dev/null | grep -q "esh-catalog-item"; do
  printf "  attempt %d/%d\r" "$((COUNTER + 1))" "$WAIT_LIMIT"
  sleep 5
  COUNTER=$((COUNTER + 1))
  if [ "$COUNTER" -ge "$WAIT_LIMIT" ]; then
    echo ""
    echo "ERROR: SUT did not become ready in time. Last container logs:"
    $COMPOSE logs --tail=30
    exit 1
  fi
done

echo ""
echo "============================================"
echo " SUT is ready!"
echo "============================================"
echo "  WebMVC      http://localhost:5100"
echo "  WebSPA      http://localhost:5104"
echo "  WebStatus   http://localhost:5107"
echo "  Identity    http://localhost:5105"
echo "  Seq (logs)  http://localhost:5340"
echo "  RabbitMQ    http://localhost:15672  (guest/guest)"
echo "  SQL Server  localhost:5433         (sa/Pass@word)"
echo ""
echo "To tear down:"
echo "  docker compose -f sut/src/docker-compose.yml \\"
echo "    -f sut/src/docker-compose.local-override.yml \\"
echo "    --env-file .retorch/envfiles/local.env \\"
echo "    -p local down --volumes"
