#!/bin/bash
# stop-monitoring.sh — kills the background sampling process started by start-monitoring.sh.
#
# Usage: stop-monitoring.sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_FILE="$SCRIPT_DIR/data/monitor.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] [WARN] No PID file at $PID_FILE — is monitoring running?"
    exit 0
fi

PID=$(cat "$PID_FILE")

if kill -0 "$PID" 2>/dev/null; then
    kill "$PID"
    rm -f "$PID_FILE"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] Monitor stopped (PID=$PID)"
else
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] [WARN] Process $PID not found — already stopped."
    rm -f "$PID_FILE"
fi

STATS="$SCRIPT_DIR/data/stats.csv"
if [ -f "$STATS" ]; then
    # Count samples: total lines minus the header
    SAMPLES=$(( $(wc -l < "$STATS") - 1 ))
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] Collected $SAMPLES measurements — generate report with:"
    echo "       python3 monitoring/generate-excel.py"
fi
