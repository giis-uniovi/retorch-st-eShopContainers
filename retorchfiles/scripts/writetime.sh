#!/bin/bash
OUTPUTDIR="$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER/$2.data"
OUTPUTDIRCOI="$WORKSPACE/retorchcostestimationdata/exec$BUILD_NUMBER/COI.data"
if [ -f "$OUTPUTDIR" ]; then
  echo -n ";$(date +%s%3N)" >>"$OUTPUTDIR"

else

  echo "tjobname;stage;COI-setup-start;COI-setup-end;setup-start;setup-end;testexec-start;testexec-end;teardown-start;teardown-end" >"$OUTPUTDIR"
  {
    echo -n "$2;$1;"
    tail -n +2 "$OUTPUTDIRCOI"
    echo -n ";$(date +%s%3N)"
  } >>"$OUTPUTDIR"

fi
