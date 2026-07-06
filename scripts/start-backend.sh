#!/bin/bash
# Start all backend services (requires Java 17 and .env variables exported)
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"

if [ -f "$ROOT/.env" ]; then
  set -a
  source "$ROOT/.env"
  set +a
fi

if [ -z "$JWT_SECRET" ]; then
  echo "ERROR: JWT_SECRET not set. Copy .env.example to .env and configure."
  exit 1
fi

start_service() {
  local name=$1
  local dir=$2
  echo "Starting $name..."
  cd "$ROOT/backend/$dir"
  mvn spring-boot:run -q &
  sleep 8
}

echo "=== Starting E-Commerce Microservices ==="

start_service "Config Server" "config-server"
start_service "Eureka Server" "eureka-server"
start_service "User Service" "user-service"
start_service "Product Service" "product-service"
start_service "Inventory Service" "inventory-service"
start_service "Payment Service" "payment-service"
start_service "Notification Service" "notification-service"
start_service "Order Service" "order-service"
start_service "API Gateway" "api-gateway"

echo ""
echo "All services starting in background."
echo "Eureka Dashboard: http://localhost:8761"
echo "API Gateway:      http://localhost:8080"
echo "Frontend:         cd frontend && npm run dev"
