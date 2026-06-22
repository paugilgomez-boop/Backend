#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
PORT="${SERVER_PORT:-8080}"

echo "=== Tower Defence Backend ==="
echo "Puerto: ${PORT}"

if ! grep -q 'Binding REST API to' src/main/java/Main.java; then
  echo ""
  echo "ERROR: Main.java en el servidor NO esta actualizado."
  echo "Sube el Main.java nuevo (debe contener 'Binding REST API to')."
  exit 1
fi

if ! grep -q '3.1.0' pom.xml; then
  echo ""
  echo "ERROR: pom.xml en el servidor NO esta actualizado."
  echo "Sube el pom.xml nuevo (exec-maven-plugin 3.1.0 y slf4j-log4j12)."
  exit 1
fi

if command -v ss >/dev/null 2>&1; then
  PORT_IN_USE="$(ss -tlnH 2>/dev/null | grep ":${PORT}\b" || true)"
  if [ -n "${PORT_IN_USE}" ]; then
    echo ""
    echo "ERROR: el puerto ${PORT} ya esta en uso:"
    echo "${PORT_IN_USE}"
    if ss -tlnp 2>/dev/null | grep -q ":${PORT}\b"; then
      ss -tlnp 2>/dev/null | grep ":${PORT}\b" || true
    fi
    echo ""
    echo "Libera el puerto (si es una instancia vuestra antigua):"
    echo "  kill <PID>"
    echo ""
    echo "O arranca en otro puerto:"
    echo "  SERVER_PORT=8081 ./start_server.sh"
    exit 1
  fi
fi

echo "Compilando y arrancando..."
echo "Para parar: Enter en esta terminal, o Ctrl+C"

export DB_HOST="${DB_HOST:-127.0.0.1}"
export DB_PORT="${DB_PORT:-3306}"
export DB_NAME="${DB_NAME:-towerdefence}"
export DB_USER="${DB_USER:-root}"
export DB_PASS="${DB_PASS:-root}"

if [ -f "./llm.env" ]; then
  # shellcheck disable=SC1091
  source "./llm.env"
fi

export LLM_MODEL="${LLM_MODEL:-grok-4-1-fast-non-reasoning}"
export LLM_URL="${LLM_URL:-https://api.x.ai/v1/chat/completions}"
export LLM_API_KEY="${LLM_API_KEY:-}"

if [ -z "${LLM_API_KEY}" ]; then
  echo ""
  echo "AVISO: LLM_API_KEY no esta definida."
  echo "Crea llm.env con: export LLM_API_KEY=xai-..."
  echo ""
elif curl -sf -X POST "${LLM_URL}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${LLM_API_KEY}" \
  -d "{\"model\":\"${LLM_MODEL}\",\"messages\":[{\"role\":\"user\",\"content\":\"hola\"}],\"stream\":false,\"max_tokens\":16}" \
  | grep -q '"content"'; then
  echo "Test xAI Grok OK con modelo ${LLM_MODEL}"
else
  echo ""
  echo "AVISO: xAI no respondio correctamente con '${LLM_MODEL}'."
  echo "Comprueba LLM_API_KEY, LLM_URL y el nombre del modelo."
  echo ""
fi

echo "Base de datos: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "LLM: ${LLM_MODEL} @ ${LLM_URL}"

mvn clean compile exec:java
