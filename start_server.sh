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

export LLM_MODEL="${LLM_MODEL:-qwen:0.5b}"
export LLM_URL="${LLM_URL:-http://127.0.0.1:11434/api/chat}"
OLLAMA_BASE="${LLM_URL%/api/chat}"
OLLAMA_BASE="${OLLAMA_BASE%/api/generate}"

if curl -sf "${OLLAMA_BASE}/api/tags" >/tmp/ollama_tags.json 2>/dev/null; then
  echo "Ollama OK en ${OLLAMA_BASE}"
  echo "Modelos instalados:"
  grep -o '"name":"[^"]*"' /tmp/ollama_tags.json | cut -d'"' -f4 || true
  if ! grep -q "\"name\":\"${LLM_MODEL}\"" /tmp/ollama_tags.json; then
    echo ""
    echo "AVISO: el modelo '${LLM_MODEL}' NO esta instalado en Ollama."
    echo "Usa el nombre exacto de 'ollama list', por ejemplo:"
    echo "  LLM_MODEL=qwen2.5:0.5b ./start_server.sh"
    echo "O instala el modelo:"
    echo "  ollama pull ${LLM_MODEL}"
    echo ""
  else
    LLM_TEST="$(curl -sf -X POST "${OLLAMA_BASE}/api/chat" \
      -H "Content-Type: application/json" \
      -d "{\"model\":\"${LLM_MODEL}\",\"messages\":[{\"role\":\"user\",\"content\":\"hola\"}],\"stream\":false}" 2>&1 || true)"
    if echo "${LLM_TEST}" | grep -q '"content"'; then
      echo "Test Ollama OK con modelo ${LLM_MODEL}"
    else
      echo ""
      echo "AVISO: Ollama no respondio correctamente con '${LLM_MODEL}':"
      echo "${LLM_TEST}" | head -c 400
      echo ""
    fi
  fi
else
  echo ""
  echo "AVISO: Ollama no responde en ${OLLAMA_BASE}"
  echo "  ss -tlnp | grep 11434"
  echo "  ollama serve"
  echo ""
fi

echo "Base de datos: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "LLM: ${LLM_MODEL} @ ${LLM_URL}"

mvn clean compile exec:java
