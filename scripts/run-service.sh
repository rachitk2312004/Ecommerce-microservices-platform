#!/bin/bash
# Run a single backend service with .env and Java 17 loaded.
# Usage: ./scripts/run-service.sh order-service
#        ./scripts/run-service.sh config-server

set -e

SERVICE="${1:?Usage: ./scripts/run-service.sh <service-dir-name>}"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"

if [ -f "$ROOT/.env" ]; then
  set -a
  # shellcheck source=/dev/null
  source "$ROOT/.env"
  set +a
  echo "Loaded environment from .env"
else
  echo "WARNING: No .env file found at $ROOT/.env"
  echo "Copy .env.example to .env and set Aiven MySQL + JWT_SECRET"
fi

if [ -z "$JWT_SECRET" ]; then
  echo "ERROR: JWT_SECRET is not set."
  exit 1
fi

if [ -z "$AIVEN_MYSQL_HOST" ]; then
  echo "ERROR: AIVEN_MYSQL_HOST is not set."
  echo "Services will try localhost:3306 and fail with 'Connection refused'."
  exit 1
fi

echo "MySQL host: $AIVEN_MYSQL_HOST:$AIVEN_MYSQL_PORT"
echo "Starting $SERVICE..."
cd "$ROOT/backend/$SERVICE"
mvn spring-boot:run
