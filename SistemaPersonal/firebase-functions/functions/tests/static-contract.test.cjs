const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..', '..');
const source = fs.readFileSync(path.join(root, 'functions', 'src', 'index.ts'), 'utf8');
const familyApi = fs.readFileSync(path.join(root, '..', 'core-network', 'src', 'main', 'java', 'com', 'sistemapersonal', 'network', 'FirebaseFamilyApi.kt'), 'utf8');
const identity = fs.readFileSync(path.join(root, '..', 'core-network', 'src', 'main', 'java', 'com', 'sistemapersonal', 'network', 'FirebaseIdentity.kt'), 'utf8');
const rules = JSON.parse(fs.readFileSync(path.join(root, 'database.rules.json'), 'utf8'));
const storage = fs.readFileSync(path.join(root, 'storage.rules'), 'utf8');

for (const name of [
  'createFamily',
  'createPairingCode',
  'redeemPairingCode',
  'leaveFamily',
  'onNuevoEvento',
  'limpiarCodigosVinculacionVencidos',
  'limpiarCapturasAntiguas',
  'limpiarEventosAntiguos',
]) {
  assert.match(source, new RegExp(`export const ${name}\\b`), `Falta ${name}`);
}

assert.match(source, /getAuth\(\)/);
assert.match(source, /setCustomUserClaims/);
assert.match(source, /transaction\(\(current\)/);
assert.match(source, /tokenEntries\[index\]\?\.uid/);
assert.match(source, /getFiles\(\{ prefix: "familias\/" \}/);
assert.match(source, /SCREENSHOT_RETENTION_MS/);
assert.match(source, /orderByChild\("timestamp"\)/);
assert.match(source, /current\.usedByUid === authContext\.uid/);
assert.match(rules.rules.familias.$familyId.eventos.$eventoId['.validate'], /app_abierta/);
assert.match(rules.rules.familias.$familyId.eventos.$eventoId['.validate'], /screenshot/);

assert.equal(rules.rules['.read'], false);
assert.equal(rules.rules['.write'], false);
assert.match(rules.rules.familias.$familyId.eventos.$eventoId['.write'], /familyId === \$familyId/);
assert.match(rules.rules.familias.$familyId.tokens.$uid['.write'], /auth.uid === \$uid/);
assert.match(storage, /allow read: if request\.auth != null/);
assert.match(storage, /allow write: if request\.auth != null/);
assert.match(storage, /image\/jpeg/);
assert.match(storage, /10 \* 1024 \* 1024/);
for (const name of ['createFamily', 'createPairingCode', 'redeemPairingCode', 'leaveFamily']) assert.match(familyApi, new RegExp(`\"${name}\"`));
assert.match(identity, /signInAnonymously/);
assert.match(identity, /getIdToken\(true\)/);

console.log('OK: contrato estático cliente/backend/reglas verificado.');
