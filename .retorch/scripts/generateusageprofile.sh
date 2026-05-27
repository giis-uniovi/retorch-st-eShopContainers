#!/bin/bash
# Copyright (c) Software Engineering Research Group (GIIS), University of Oviedo.
# All intellectual property rights on this file belong to GIIS.
# The generateusageprofile.sh script builds the usage profile of the execution (one self-contained HTML file with the
# resources used and what they would cost in different clouds) from the single usage file written by
# resourcemonitor.sh (artifacts/usageprofile-<branch>-<build>.csv).
#
# The profiling tool is the jar $HOME/.retorch/retorch-profiling-cli.jar. If it is not there it is downloaded from the
# URL configured in retorchCI.properties (profiling-jar-url). A project can prepare the jar by other means (e.g. build
# it from source while no release publishes it) in the optional custom-usageprofile snippet, inserted just below.
# The prices of the cloud providers are downloaded and cached in $HOME/.retorch/prices.json. The regions (comma
# separated lists) come from retorchCI.properties (azure-region, aws-region) and can be overridden with the
# AZURE_REGIONS / AWS_REGIONS environment variables (e.g. Jenkins job parameters); an empty value disables the provider.

PROFILING_JAR_URL="https://github.com/giis-uniovi/retorch/releases/latest/download/retorch-profiling-cli.jar"
PROFILING_JAR="$HOME/.retorch/retorch-profiling-cli.jar"

fail() {
    "$SCRIPTS_FOLDER/printLog.sh" "ERROR" "UsageProfile" "$1"
    exit 1
}

mkdir -p "$HOME/.retorch"

# START Custom usage profile commands (they can leave the profiling jar in $PROFILING_JAR)
"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "UsageProfile" "Start executing custom commands"
# TEMPORARY. Until a release publishes retorch-profiling-cli.jar, the profiling tool is built from source here.
# DELETE THIS FILE when the release exists: generateusageprofile.sh then downloads the released jar by itself
# (profiling-jar-url in retorchCI.properties).
#
# This snippet is inserted in generateusageprofile.sh, which already defines $PROFILING_JAR (where the tool is looked
# for) and fail(). It clones the repository, builds the profiling module (only when the branch has new commits) and
# leaves the jar in $PROFILING_JAR. It needs git, mvn and java in the agent.
RETORCH_REPO="https://github.com/giis-uniovi/retorch.git"
RETORCH_BRANCH="211-retorch-orchestration-package-improvement"
RETORCH_SRC="$HOME/.retorch/retorch-src"

if [ ! -d "$RETORCH_SRC/.git" ]; then
    git clone --quiet "$RETORCH_REPO" "$RETORCH_SRC" || fail "Could not clone $RETORCH_REPO"
fi
git -C "$RETORCH_SRC" fetch --quiet origin "$RETORCH_BRANCH" || fail "Could not fetch the branch $RETORCH_BRANCH"
git -C "$RETORCH_SRC" checkout --quiet -f -B "$RETORCH_BRANCH" "origin/$RETORCH_BRANCH" || fail "Could not checkout $RETORCH_BRANCH"

RETORCH_COMMIT=$(git -C "$RETORCH_SRC" rev-parse HEAD)
if [ ! -f "$RETORCH_SRC/.built-$RETORCH_COMMIT" ] || [ ! -f "$PROFILING_JAR" ]; then
    "$SCRIPTS_FOLDER/printLog.sh" "INFO" "UsageProfile" "Building the profiling tool ($RETORCH_BRANCH, $RETORCH_COMMIT)"
    rm -f "$RETORCH_SRC"/.built-*
    mvn -q -B -f "$RETORCH_SRC/pom.xml" -pl retorch-profiling -am package -Dmaven.test.skip=true \
        || fail "The build of the profiling tool failed"
    BUILT_JAR=$(ls "$RETORCH_SRC"/retorch-profiling/target/retorch-profiling-*-cli.jar 2>/dev/null | head -1)
    [ -n "$BUILT_JAR" ] || fail "The profiling jar was not built"
    cp "$BUILT_JAR" "$PROFILING_JAR"
    touch "$RETORCH_SRC/.built-$RETORCH_COMMIT"
fi

"$SCRIPTS_FOLDER/printLog.sh" "DEBUG" "UsageProfile" "End executing custom commands"
# END Custom usage profile commands

if [ ! -f "$PROFILING_JAR" ]; then
    "$SCRIPTS_FOLDER/printLog.sh" "INFO" "UsageProfile" "Downloading the profiling tool from $PROFILING_JAR_URL"
    curl -fsSL "$PROFILING_JAR_URL" -o "$PROFILING_JAR.tmp" || { rm -f "$PROFILING_JAR.tmp"; fail "Could not download $PROFILING_JAR_URL"; }
    mv "$PROFILING_JAR.tmp" "$PROFILING_JAR"
fi

USAGE_FILE="$WORKSPACE/artifacts/usageprofile-${BRANCH_NAME///}-${BUILD_NUMBER}.csv"
OUTPUT_FILE="$WORKSPACE/artifacts/usageprofile/usageprofile.html"
[ -f "$USAGE_FILE" ] || fail "Missing $USAGE_FILE"

# The execution graph (TJobs, stages and tests) written by the orchestrator, if the project has it
PLAN_FILE="$WORKSPACE/.retorch/executionplan.json"
PLAN_ARG=()
[ -f "$PLAN_FILE" ] && PLAN_ARG=("$PLAN_FILE")

java -jar "$PROFILING_JAR" "${JOB_NAME}-${BUILD_NUMBER}" "$USAGE_FILE" "$OUTPUT_FILE" \
    "${AZURE_REGIONS:-westeurope}" "${AWS_REGIONS:-eu-west-1}" \
    "$HOME/.retorch/prices.json" "${PLAN_ARG[@]}"
