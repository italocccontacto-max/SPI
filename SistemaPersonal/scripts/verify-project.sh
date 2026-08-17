#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

required=(
  gradlew
  gradlew.bat
  gradle/wrapper/gradle-wrapper.jar
  gradle/wrapper/gradle-wrapper.properties
  firebase.json
  firebase-functions/functions/package.json
  firebase-functions/functions/src/index.ts
  firebase-functions/database.rules.json
  firebase-functions/storage.rules
)

for file in "${required[@]}"; do
  test -f "$file" || { echo "ERROR: falta $file" >&2; exit 1; }
done

test ! -f local.properties || { echo "ERROR: local.properties no debe formar parte de la entrega" >&2; exit 1; }
test ! -d firebase-functions/functions/node_modules || { echo "ERROR: node_modules no debe formar parte de la entrega" >&2; exit 1; }
test ! -d firebase-functions/functions/lib || { echo "ERROR: lib generada no debe formar parte de la entrega fuente" >&2; exit 1; }

if grep -RIlE 'FIREBASE_(API_KEY|APP_ID|PROJECT_ID|DATABASE_URL|STORAGE_BUCKET)=[^[:space:]]+' . \
  --exclude-dir=.git --exclude='local.properties.example' --exclude='README.md' --exclude='AUDITORIA-FINAL.md' --exclude='PASOS.md'; then
  echo "ERROR: parece haber credenciales Firebase incrustadas." >&2
  exit 1
fi

node firebase-functions/functions/tests/static-contract.test.cjs

echo "OK: estructura, saneamiento y contrato estático verificados."
