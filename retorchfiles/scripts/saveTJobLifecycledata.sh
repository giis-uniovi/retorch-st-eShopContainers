#!/bin/bash
PATH_FILES="$WORKSPACE/retorchcostestimationdata/exec${BUILD_NUMBER}/tjob*"
OUTPUT_FILE="$WORKSPACE/artifacts/lifecycletimmingsexec${BRANCH_NAME///}-${BUILD_NUMBER}.csv"
OUTPUTDIRCOI="$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER/COI.data"
echo "tjobname;stage;COI-setup-start;COI-setup-end;tjob-setup-start;tjob-setup-end;tjob-testexec-start;tjob-testexec-end;tjob-teardown-start;tjob-teardown-end;coi-teardown-start;coi-teardown-end" >"$OUTPUT_FILE"
for csvfile in $PATH_FILES; do
  {
    tail -n +2 "$csvfile"
    echo -n ";"
    tail -n +2 "$OUTPUTDIRCOI" | rev | cut -d ';' -f 2 | rev | tr -d '\n'
    echo -n ";"
    tail -n +2 "$OUTPUTDIRCOI" | rev | cut -d ';' -f 1 | rev
  } >>"$OUTPUT_FILE"
done
