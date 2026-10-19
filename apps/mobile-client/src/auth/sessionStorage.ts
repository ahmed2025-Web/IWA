import { Platform } from 'react-native';
import * as SecureStore from 'expo-secure-store';

export type StoredSession = {
  accessToken: string;
  refreshToken?: string;
  idToken?: string;
  issuedAt: number;
  expiresIn?: number;
};

const sessionKey = 'okit.auth.session';

export function isSessionValid(session: StoredSession) {
  if (!session.expiresIn) {
    return true;
  }

  const expiresAt = (session.issuedAt + session.expiresIn) * 1000;
  return expiresAt > Date.now() + 30_000;
}

export async function loadStoredSession(): Promise<StoredSession | null> {
  if (Platform.OS === 'web' || !(await SecureStore.isAvailableAsync())) {
    return null;
  }

  const serialized = await SecureStore.getItemAsync(sessionKey);
  if (!serialized) {
    return null;
  }

  try {
    const session = JSON.parse(serialized) as StoredSession;
    if (!session.accessToken || !session.issuedAt || !isSessionValid(session)) {
      await SecureStore.deleteItemAsync(sessionKey);
      return null;
    }
    return session;
  } catch {
    await SecureStore.deleteItemAsync(sessionKey);
    return null;
  }
}

export async function saveStoredSession(session: StoredSession) {
  if (Platform.OS === 'web' || !(await SecureStore.isAvailableAsync())) {
    return;
  }

  await SecureStore.setItemAsync(sessionKey, JSON.stringify(session));
}

export async function clearStoredSession() {
  if (Platform.OS === 'web' || !(await SecureStore.isAvailableAsync())) {
    return;
  }

  await SecureStore.deleteItemAsync(sessionKey);
}
