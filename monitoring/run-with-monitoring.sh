#!/bin/bash
# run-with-monitoring.sh — runs a command with resource monitoring around it,
# then generates the Excel report automatically.
#
# Usage:
#   run-with-monitoring.sh [INTERVAL_SECONDS] <command>
#
# Examples:
#   bash monitoring/run-with-monitoring.sh "mvn test"
#   bash monitoring/run-with-monitoring.sh 10 "mvn test -Dtest=WebSPACatalogTests"
#
# The interval defaults to 5 seconds when not provided.
# The Excel report is written to monitoring/data/resource-report.xlsx.

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 [INTERVAL_SECONDS] <command>"
    echo "Example: $0 5 \"mvn test\""
    exit 1
fi

# Treat the first argument as the interval if it is a plain integer
if echo "$1" | grep -qE '^[0-9]+$'; then
    INTERVAL="$1"
    shift
else
    INTERVAL=5
fi

COMMAND="$*"

# ── Start monitoring ──────────────────────────────────────────────────────────
bash "$SCRIPT_DIR/start-monitoring.sh" "$INTERVAL"

# ── Execute the test suite ────────────────────────────────────────────────────
echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] Running: $COMMAND"
# Run in a subshell so a test failure does not skip the cleanup steps below
set +e
eval "$COMMAND"
CMD_EXIT=$?
set -e

# ── Stop monitoring ───────────────────────────────────────────────────────────
bash "$SCRIPT_DIR/stop-monitoring.sh"

# ── Generate Excel report ─────────────────────────────────────────────────────
REPORT="$SCRIPT_DIR/data/resource-report.xlsx"
echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] Generating Excel report ..."
python3 "$SCRIPT_DIR/generate-excel.py" \
    --data-dir "$SCRIPT_DIR/data" \
    --output   "$REPORT"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] Report written to $REPORT"
exit $CMD_EXIT
