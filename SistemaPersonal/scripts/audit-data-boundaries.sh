#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
fail=0
check_absent() {
  local label="$1"; shift
  if grep -RIn --include='*.kt' "$@" app-personal core-domain >/dev/null 2>&1; then
    echo "FAIL  $label"
    grep -RIn --include='*.kt' "$@" app-personal core-domain || true
    fail=1
  else
    echo "PASS  $label"
  fi
}
check_present() {
  local label="$1"; shift
  if grep -RIn --include='*.kt' "$@" core-data/src/main/java/com/sistemapersonal/data/repo/SistemaPersonalRepository.kt >/dev/null 2>&1; then
    echo "PASS  $label"
  else
    echo "FAIL  $label"
    fail=1
  fi
}

check_absent 'RoomDatabase no se expone mediante repo.db' 'repo\.db\.'
check_absent 'SistemaPersonalDatabase no se expone como propiedad pública del repository' '^[[:space:]]*(val|var)[[:space:]]+db:[[:space:]]*SistemaPersonalDatabase'

if grep -RIn --include='*.kt' 'SistemaPersonalDatabase' app-personal core-domain >/tmp/direct-db-refs.txt 2>/dev/null; then
  echo 'FAIL  app/domain no acceden directamente a SistemaPersonalDatabase'
  cat /tmp/direct-db-refs.txt
  fail=1
else
  echo 'PASS  app/domain no acceden directamente a SistemaPersonalDatabase'
fi
check_present 'Screenshot cleanup pasa por DAO controlado por repository' 'val dao = screenshotDao\(\)'
check_present_fixed() {
  local label="$1"; local needle="$2";
  if grep -RInF "$needle" core-data/src/main/java/com/sistemapersonal/data/repo/SistemaPersonalRepository.kt app-personal/src/main/java/com/centinela/app/capture/ScreenshotCaptureService.kt >/dev/null 2>&1; then
    echo "PASS  $label"
  else
    echo "FAIL  $label"
    fail=1
  fi
}
check_present_fixed 'Fallos de filesystem no borran metadata Room indiscriminadamente' 'if (borrado) dao.eliminar(captura)'
check_present_fixed 'Fallo al insertar metadata elimina el JPEG recién creado' 'file?.let { runCatching { it.delete() } }'

echo
echo "Data-boundary source audit: $([ $fail -eq 0 ] && echo PASS || echo FAIL)"
exit "$fail"
