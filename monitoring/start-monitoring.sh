#!/bin/bash
# start-monitoring.sh — samples docker stats for every running container at a fixed interval.
# Writes one CSV row per container per tick to monitoring/data/stats.csv.
#
# Usage: start-monitoring.sh [INTERVAL_SECONDS]
#   INTERVAL_SECONDS  sampling interval, default 5

INTERVAL="${1:-5}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DATA_DIR="$SCRIPT_DIR/data"
OUTPUT_FILE="$DATA_DIR/stats.csv"
PID_FILE="$DATA_DIR/monitor.pid"

mkdir -p "$DATA_DIR"

# Guard against a stale or duplicate run
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] [WARN] Monitor already running (PID=$OLD_PID). Run stop-monitoring.sh first."
        exit 1
    fi
    rm -f "$PID_FILE"
fi

# Write CSV header
echo "timestamp,container,cpu_pct,mem_usage,mem_pct,net_io,block_io,pids" > "$OUTPUT_FILE"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] Starting resource monitor (interval=${INTERVAL}s) ..."
echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] Output → $OUTPUT_FILE"

# Background sampling loop — does NOT inherit set -e from caller
(
    while true; do
        TIMESTAMP="$(date '+%Y-%m-%d %H:%M:%S')"
        IDS="$(docker ps -q 2>/dev/null)"
        if [ -n "$IDS" ]; then
            # SC2086: intentional word-splitting of IDS
            # shellcheck disable=SC2086
            docker stats --no-stream \
                --format "{{.Name}},{{.CPUPerc}},{{.MemUsage}},{{.MemPerc}},{{.NetIO}},{{.BlockIO}},{{.PIDs}}" \
                $IDS 2>/dev/null \
            | while IFS= read -r line; do
                echo "${TIMESTAMP},${line}" >> "$OUTPUT_FILE"
              done
        fi
        sleep "$INTERVAL"
    done
) &

MONITOR_PID=$!
echo "$MONITOR_PID" > "$PID_FILE"
echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] Monitor PID=$MONITOR_PID — stop with: bash monitoring/stop-monitoring.sh"
