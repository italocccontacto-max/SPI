import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getDatabase } from "firebase-admin/database";
import { getMessaging } from "firebase-admin/messaging";
import { getStorage } from "firebase-admin/storage";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { onValueCreated } from "firebase-functions/v2/database";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { logger } from "firebase-functions";
import { randomBytes } from "node:crypto";

initializeApp();

const db = getDatabase();
const storage = getStorage().bucket();
const auth = getAuth();
const messaging = getMessaging();
const REGION = "us-central1";
const PAIRING_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
const PAIRING_CODE_LENGTH = 12;
const PAIRING_TTL_MS = 15 * 60 * 1000;
const SCREENSHOT_RETENTION_MS = 30 * 24 * 60 * 60 * 1000;

interface EventoRemoto {
  tipo: string;
  timestamp: number;
  resumen: string;
  extra?: Record<string, unknown>;
}

const TITULOS_POR_TIPO: Record<string, string> = {
  app_abierta: "Sistema Personal",
  racha: "Racha",
  logro: "¡Logro desbloqueado!",
  fallo: "Aviso",
  screenshot: "Nueva captura relevante",
};

function randomId(bytes = 18): string {
  return randomBytes(bytes).toString("base64url");
}

function randomPairingCode(): string {
  const bytes = randomBytes(PAIRING_CODE_LENGTH);
  return Array.from(bytes, (b) => PAIRING_CODE_ALPHABET[b % PAIRING_CODE_ALPHABET.length]).join("");
}

function assertAuth(request: { auth?: { uid: string; token: Record<string, unknown> } | null }) {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "La operación requiere autenticación.");
  }
  return request.auth;
}

async function setFamilyClaims(uid: string, familyId?: string, familyRole?: string) {
  const user = await auth.getUser(uid);
  const current = { ...(user.customClaims || {}) };
  if (familyId) current.familyId = familyId;
  else delete current.familyId;
  if (familyRole) current.familyRole = familyRole;
  else delete current.familyRole;
  await auth.setCustomUserClaims(uid, current);
}

async function ensureUnusedPairingCode(): Promise<string> {
  for (let attempt = 0; attempt < 5; attempt += 1) {
    const code = randomPairingCode();
    const snapshot = await db.ref(`pairingCodes/${code}`).get();
    if (!snapshot.exists()) return code;
  }
  throw new HttpsError("resource-exhausted", "No se pudo generar un código único.");
}

export const createFamily = onCall({ region: REGION }, async (request) => {
  const authContext = assertAuth(request);
  const existingFamilyId = authContext.token?.familyId as string | undefined;
  if (existingFamilyId) {
    throw new HttpsError("failed-precondition", "Esta identidad ya pertenece a una familia.");
  }

  const now = Date.now();
  const familyId = randomId();
  const pairingCode = await ensureUnusedPairingCode();
  const expiresAt = now + PAIRING_TTL_MS;

  const family = {
    ownerUid: authContext.uid,
    createdAt: now,
    members: {
      [authContext.uid]: {
        role: "owner",
        createdAt: now,
      },
    },
  };

  await db.ref().update({
    [`familias/${familyId}`]: family,
    [`pairingCodes/${pairingCode}`]: {
      familyId,
      ownerUid: authContext.uid,
      createdAt: now,
      expiresAt,
    },
  });

  try {
    await setFamilyClaims(authContext.uid, familyId, "owner");
  } catch (error) {
    await db.ref().update({
      [`familias/${familyId}`]: null,
      [`pairingCodes/${pairingCode}`]: null,
    });
    throw error;
  }

  return { familyId, pairingCode, expiresAt };
});

export const createPairingCode = onCall({ region: REGION }, async (request) => {
  const authContext = assertAuth(request);
  const familyId = authContext.token?.familyId as string | undefined;
  const role = authContext.token?.familyRole as string | undefined;
  if (!familyId || role !== "owner") {
    throw new HttpsError("permission-denied", "Solo el propietario de la familia puede generar códigos.");
  }

  const familySnapshot = await db.ref(`familias/${familyId}`).get();
  if (!familySnapshot.exists()) {
    throw new HttpsError("not-found", "La familia ya no existe.");
  }

  const pairingCode = await ensureUnusedPairingCode();
  const now = Date.now();
  const expiresAt = now + PAIRING_TTL_MS;
  const oldCodesSnapshot = await db.ref("pairingCodes")
    .orderByChild("familyId")
    .equalTo(familyId)
    .get();
  const updates: Record<string, unknown> = {};
  oldCodesSnapshot.forEach((child) => {
    updates[`pairingCodes/${child.key}`] = null;
    return false;
  });
  updates[`pairingCodes/${pairingCode}`] = {
    familyId,
    ownerUid: authContext.uid,
    createdAt: now,
    expiresAt,
  };
  await db.ref().update(updates);
  return { pairingCode, expiresAt, familyId };
});

export const redeemPairingCode = onCall({ region: REGION }, async (request) => {
  const authContext = assertAuth(request);
  const pairingCode = String((request.data as { pairingCode?: unknown })?.pairingCode ?? "")
    .trim()
    .toUpperCase();
  const existingFamilyId = authContext.token?.familyId as string | undefined;

  if (!/^[A-Z0-9]{12}$/.test(pairingCode)) {
    throw new HttpsError("invalid-argument", "El código no tiene el formato esperado.");
  }
  if (existingFamilyId) {
    throw new HttpsError("failed-precondition", "La identidad ya pertenece a una familia. Desvinculala antes de cambiar.");
  }

  const codeRef = db.ref(`pairingCodes/${pairingCode}`);
  const codeSnapshot = await codeRef.get();
  if (!codeSnapshot.exists()) {
    throw new HttpsError("not-found", "Código inexistente o ya utilizado.");
  }

  const invite = codeSnapshot.val() as {
    familyId: string;
    ownerUid: string;
    expiresAt: number;
  };

  if (invite.expiresAt <= Date.now()) {
    await codeRef.remove();
    throw new HttpsError("deadline-exceeded", "El código de vinculación venció.");
  }

  const familySnapshot = await db.ref(`familias/${invite.familyId}`).get();
  if (!familySnapshot.exists()) {
    await codeRef.remove();
    throw new HttpsError("not-found", "La familia ya no existe.");
  }

  const claimed = await codeRef.transaction((current) => {
    if (!current) return;

    if (current.usedAt) {
      if (current.usedByUid === authContext.uid) return current;
      return;
    }
    return { ...current, usedAt: Date.now(), usedByUid: authContext.uid };
  });

  if (!claimed.committed) {
    throw new HttpsError("already-exists", "El código ya fue utilizado por otro usuario.");
  }

  const now = Date.now();

  await db.ref(`familias/${invite.familyId}/members/${authContext.uid}`).set({
    role: "member",
    createdAt: now,
  });
  await setFamilyClaims(authContext.uid, invite.familyId, "member");
  await codeRef.remove();
  return { familyId: invite.familyId };
});

export const leaveFamily = onCall({ region: REGION }, async (request) => {
  const authContext = assertAuth(request);
  const familyId = authContext.token?.familyId as string | undefined;
  const role = authContext.token?.familyRole as string | undefined;
  if (!familyId || role !== "member") return { left: false };

  await db.ref().update({
    [`familias/${familyId}/members/${authContext.uid}`]: null,
    [`familias/${familyId}/tokens/${authContext.uid}`]: null,
  });
  await setFamilyClaims(authContext.uid);
  return { left: true };
});

export const onNuevoEvento = onValueCreated(
  { ref: "/familias/{familyId}/eventos/{eventoId}", region: REGION },
  async (event) => {
    const familyId = event.params.familyId;
    const evento = event.data.val() as EventoRemoto | null;
    if (!evento?.resumen) {
      logger.warn("Evento sin resumen; se omite push", { familyId });
      return;
    }

    const tokensSnap = await db.ref(`/familias/${familyId}/tokens`).get();
    if (!tokensSnap.exists()) return;

    const tokenEntries = Object.entries(tokensSnap.val() as Record<string, string>)
      .filter(([, token]) => typeof token === "string" && token.length > 0)
      .map(([uid, token]) => ({ uid, token }));
    if (tokenEntries.length === 0) return;

    const response = await messaging.sendEachForMulticast({
      tokens: tokenEntries.map((entry) => entry.token),
      notification: {
        title: TITULOS_POR_TIPO[evento.tipo] ?? "Sistema Personal",
        body: evento.resumen,
      },
      data: {
        tipo: evento.tipo,
        timestamp: String(evento.timestamp ?? Date.now()),
      },
      android: { priority: "high" },
    });

    const invalidTokenDeletes: Promise<unknown>[] = [];
    response.responses.forEach((result, index) => {
      const code = result.error?.code;
      if (!result.success && (code === "messaging/registration-token-not-registered" || code === "messaging/invalid-registration-token")) {
        const uid = tokenEntries[index]?.uid;
        if (uid) invalidTokenDeletes.push(db.ref(`/familias/${familyId}/tokens/${uid}`).remove());
      }
    });
    await Promise.allSettled(invalidTokenDeletes);

    logger.info("Push de evento completado", {
      familyId,
      tipo: evento.tipo,
      successCount: response.successCount,
      failureCount: response.failureCount,
    });
  },
);

export const limpiarCodigosVinculacionVencidos = onSchedule(
  { schedule: "every hour", timeZone: "UTC", region: REGION },
  async () => {
    const snapshot = await db.ref("pairingCodes").get();
    if (!snapshot.exists()) return;
    const now = Date.now();
    const updates: Record<string, null> = {};
    snapshot.forEach((child) => {
      const expiresAt = child.child("expiresAt").val();
      if (typeof expiresAt === "number" && expiresAt <= now) {
        updates[`pairingCodes/${child.key}`] = null;
      }
      return false;
    });
    if (Object.keys(updates).length > 0) await db.ref().update(updates);
    logger.info("Limpieza de códigos de vinculación completada", { eliminados: Object.keys(updates).length });
  },
);

export const limpiarEventosAntiguos = onSchedule(
  { schedule: "every day 03:35", timeZone: "UTC", region: REGION },
  async () => {
    const familiasSnapshot = await db.ref("familias").get();
    if (!familiasSnapshot.exists()) return;

    const cutoff = Date.now() - SCREENSHOT_RETENTION_MS;
    const updates: Record<string, null> = {};

    const familyIds: string[] = [];
    familiasSnapshot.forEach((family) => {
      if (family.key) familyIds.push(family.key);
      return false;
    });

    await Promise.all(
      familyIds.map(async (familyId) => {
        const oldEvents = await db.ref(`familias/${familyId}/eventos`)
          .orderByChild("timestamp")
          .endAt(cutoff)
          .get();
        oldEvents.forEach((event) => {
          if (event.key) updates[`familias/${familyId}/eventos/${event.key}`] = null;
          return false;
        });
      }),
    );

    if (Object.keys(updates).length > 0) {
      await db.ref().update(updates);
    }
    logger.info("Limpieza de eventos remotos completada", { eliminados: Object.keys(updates).length });
  },
);

export const limpiarCapturasAntiguas = onSchedule(
  { schedule: "every day 03:20", timeZone: "UTC", region: REGION },
  async () => {
    const [files] = await storage.getFiles({ prefix: "familias/" });
    const cutoff = Date.now() - SCREENSHOT_RETENTION_MS;
    const deletions = files
      .filter((file) => /\/screenshots\/\d+\.jpg$/.test(file.name))
      .filter((file) => Number(file.name.match(/(\d+)\.jpg$/)?.[1] ?? 0) < cutoff)
      .map((file) => file.delete());
    await Promise.allSettled(deletions);
    logger.info("Limpieza de capturas completada", { eliminadas: deletions.length });
  },
);
