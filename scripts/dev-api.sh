#!/usr/bin/env bash
# LifeOS Local Development API Launcher
#
# Purpose:
# Starts the Spring Boot backend in apps/api with the dedicated 'telegram-echo'
# profile for local development and testing of Telegram echo interactions.
#
# Environment configuration (.env) & Precedence:
# Resolves the repository root relative to this script and loads the root .env file.
# Sourced directly using Bash with automatic variable export enabled ('set -a').
# The .env file must contain trusted, Bash-compatible assignments: KEY='value'.
# Root .env assignments become exported variables in the launched child process;
# unrelated inherited environment variables remain intact; parent shell state
# and file values on disk are not rewritten.
# Command tracing is explicitly disabled, and stderr during sourcing is suppressed
# to prevent raw interpreter diagnostics from leaking sensitive values (such as
# TELEGRAM_BOT_TOKEN) into terminal or trace logs.
#
# Prerequisites:
# - Bash shell.
# - JDK 25 (or compatible newer major): both runtime ('java') and compiler ('javac')
#   located via JAVA_HOME or available on PATH.
# - TELEGRAM_BOT_TOKEN: must be non-blank in .env.
#
# Maven Wrapper:
# Executes 'sh ./mvnw spring-boot:run -Dspring-boot.run.profiles=telegram-echo'.
# First use of the wrapper will download the Maven distribution (Maven 3.10.0 per
# wrapper configuration) and resolve project dependencies from Maven repositories.
set -euo pipefail

# Ensure command tracing is disabled to protect credentials from trace logs
{ set +x; } 2>/dev/null

# Resolve repository root directory reliably from script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

ENV_FILE="${REPO_ROOT}/.env"

# Verify .env file existence
if [ ! -f "${ENV_FILE}" ]; then
  echo "Error: Configuration file '${ENV_FILE}' not found." >&2
  echo "Please configure .env before running this launcher." >&2
  exit 1
fi

# Load root .env with automatic variable export; suppress interpreter errors to prevent secret leakage
{ set +x; } 2>/dev/null
set -a
# shellcheck source=/dev/null
if ! { . "${ENV_FILE}"; } 2>/dev/null; then
  set +a
  echo "Error: Failed to load configuration from '${ENV_FILE}'." >&2
  echo "Verify that the file contains valid Bash-compatible assignments (e.g. KEY='value')." >&2
  exit 1
fi
set +a
{ set +x; } 2>/dev/null

# Validate required credential variable without printing any secret values
if [ -z "${TELEGRAM_BOT_TOKEN:-}" ] || [ -z "${TELEGRAM_BOT_TOKEN//[[:space:]]/}" ]; then
  echo "Error: TELEGRAM_BOT_TOKEN is missing or blank." >&2
  echo "Please configure TELEGRAM_BOT_TOKEN in .env with a valid bot token." >&2
  exit 1
fi

# Locate Java runtime (java) and compiler (javac) under JAVA_HOME or PATH
if [ -n "${JAVA_HOME:-}" ]; then
  if [ ! -x "${JAVA_HOME}/bin/java" ]; then
    echo "Error: JAVA_HOME is set, but '\$JAVA_HOME/bin/java' is not executable. JDK 25 is required." >&2
    exit 1
  fi
  if [ ! -x "${JAVA_HOME}/bin/javac" ]; then
    echo "Error: JAVA_HOME is set, but '\$JAVA_HOME/bin/javac' is not executable. JDK 25 is required." >&2
    exit 1
  fi
  JAVACMD="${JAVA_HOME}/bin/java"
  JAVAC_CMD="${JAVA_HOME}/bin/javac"
else
  if ! command -v java >/dev/null 2>&1; then
    echo "Error: java executable not found on PATH and JAVA_HOME is not set. JDK 25 is required." >&2
    echo "Please install JDK 25 and configure JAVA_HOME or ensure java is on PATH." >&2
    exit 1
  fi
  if ! command -v javac >/dev/null 2>&1; then
    echo "Error: javac compiler not found on PATH and JAVA_HOME is not set. JDK 25 is required." >&2
    echo "Please install JDK 25 and configure JAVA_HOME or ensure javac is on PATH." >&2
    exit 1
  fi
  JAVACMD="java"
  JAVAC_CMD="javac"
fi

# Inspect runtime version (java) against Java 25 target (25 or compatible newer major)
JAVA_VER_OUTPUT="$("$JAVACMD" -version 2>&1 || true)"
JAVA_MAJOR=""
if [[ "$JAVA_VER_OUTPUT" =~ \"([0-9]+)(\.|\-|\") ]]; then
  JAVA_MAJOR="${BASH_REMATCH[1]}"
elif [[ "$JAVA_VER_OUTPUT" =~ version[[:space:]]+([0-9]+) ]]; then
  JAVA_MAJOR="${BASH_REMATCH[1]}"
fi

if [ -z "$JAVA_MAJOR" ] || (( JAVA_MAJOR < 25 )); then
  echo "Error: Incompatible Java runtime version (detected major version: '${JAVA_MAJOR:-unknown}')." >&2
  echo "Target release requires JDK 25 or a compatible newer major version." >&2
  exit 1
fi

# Inspect compiler version (javac) against Java 25 target (25 or compatible newer major)
JAVAC_VER_OUTPUT="$("$JAVAC_CMD" -version 2>&1 || true)"
JAVAC_MAJOR=""
if [[ "$JAVAC_VER_OUTPUT" =~ javac[[:space:]]+([0-9]+)(\.|\-|$) ]]; then
  JAVAC_MAJOR="${BASH_REMATCH[1]}"
elif [[ "$JAVAC_VER_OUTPUT" =~ ([0-9]+)(\.|\-|$) ]]; then
  JAVAC_MAJOR="${BASH_REMATCH[1]}"
fi

if [ -z "$JAVAC_MAJOR" ] || (( JAVAC_MAJOR < 25 )); then
  echo "Error: Incompatible Java compiler version (detected major version: '${JAVAC_MAJOR:-unknown}')." >&2
  echo "Target release requires JDK 25 or a compatible newer major version." >&2
  exit 1
fi

# Change directory to backend application
API_DIR="${REPO_ROOT}/apps/api"
if [ ! -d "${API_DIR}" ]; then
  echo "Error: API directory '${API_DIR}' not found." >&2
  exit 1
fi

cd "${API_DIR}"

if [ ! -f "./mvnw" ]; then
  echo "Error: Maven Wrapper (mvnw) not found in '${API_DIR}'." >&2
  exit 1
fi

# Start API via Maven Wrapper with dedicated telegram-echo profile
# Fixed launch command: token passed securely via exported environment (TELEGRAM_BOT_TOKEN).
# Passthrough argument forwarding is omitted to preserve the fixed telegram-echo route.
export SPRING_PROFILES_ACTIVE="telegram-echo"
exec sh ./mvnw spring-boot:run -Dspring-boot.run.profiles=telegram-echo
