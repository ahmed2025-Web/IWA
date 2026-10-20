import {
  createContext,
  PropsWithChildren,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import * as AuthSession from 'expo-auth-session';
import * as WebBrowser from 'expo-web-browser';

import {
  clientId,
  createRedirectUri,
  oidcDiscovery,
  registrationDiscovery,
} from './config';
import {
  clearStoredSession,
  loadStoredSession,
  saveStoredSession,
  StoredSession,
} from './sessionStorage';

WebBrowser.maybeCompleteAuthSession();

type KeycloakUser = {
  sub: string;
  email?: string;
  preferred_username?: string;
  given_name?: string;
  family_name?: string;
};

type KeycloakAccessToken = {
  realm_access?: {
    roles?: string[];
  };
};

type AuthContextValue = {
  accessToken: string | null;
  canAuthenticate: boolean;
  error: string | null;
  isBusy: boolean;
  isReady: boolean;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  registerAccount: () => Promise<void>;
  user: KeycloakUser | null;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function toStoredSession(tokens: AuthSession.TokenResponse): StoredSession {
  return {
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    idToken: tokens.idToken,
    issuedAt: tokens.issuedAt,
    expiresIn: tokens.expiresIn,
  };
}

function readRealmRoles(accessToken: string): string[] {
  try {
    const encodedPayload = accessToken.split('.')[1];
    if (!encodedPayload) {
      return [];
    }
    const normalized = encodedPayload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
    const payload = JSON.parse(atob(padded)) as KeycloakAccessToken;
    return payload.realm_access?.roles ?? [];
  } catch {
    return [];
  }
}

function requireBuyer(accessToken: string) {
  const roles = readRealmRoles(accessToken);
  if (!roles.includes('BUYER')) {
    throw new Error("Ce compte n'est pas autorisé à utiliser l'application acheteur.");
  }
}

async function loadUser(accessToken: string): Promise<KeycloakUser> {
  const response = await fetch(oidcDiscovery.userInfoEndpoint!, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });

  if (!response.ok) {
    throw new Error(`Impossible de lire l'identité (${response.status}).`);
  }

  return response.json() as Promise<KeycloakUser>;
}

export function AuthProvider({ children }: PropsWithChildren) {
  const redirectUri = useMemo(() => createRedirectUri(), []);
  const handledCodes = useRef(new Set<string>());
  const [session, setSession] = useState<StoredSession | null>(null);
  const [user, setUser] = useState<KeycloakUser | null>(null);
  const [isReady, setIsReady] = useState(false);
  const [isBusy, setIsBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const authRequestConfig: AuthSession.AuthRequestConfig = useMemo(
    () => ({
      clientId,
      redirectUri,
      scopes: ['openid', 'profile', 'email'],
      responseType: AuthSession.ResponseType.Code,
      usePKCE: true,
      codeChallengeMethod: AuthSession.CodeChallengeMethod.S256,
    }),
    [redirectUri]
  );

  const [loginRequest, , promptLogin] = AuthSession.useAuthRequest(
    authRequestConfig,
    oidcDiscovery
  );
  const [registerRequest, , promptRegistration] =
    AuthSession.useAuthRequest(authRequestConfig, registrationDiscovery);

  const establishSession = useCallback(async (storedSession: StoredSession) => {
    requireBuyer(storedSession.accessToken);
    const identity = await loadUser(storedSession.accessToken);
    await saveStoredSession(storedSession);
    setSession(storedSession);
    setUser(identity);
  }, []);

  useEffect(() => {
    let active = true;

    loadStoredSession()
      .then(async (storedSession) => {
        if (storedSession && active) {
          await establishSession(storedSession);
        }
      })
      .catch(async (restoreError: unknown) => {
        await clearStoredSession();
        if (active && restoreError instanceof Error) {
          setError(restoreError.message);
        }
      })
      .finally(() => {
        if (active) {
          setIsReady(true);
        }
      });

    return () => {
      active = false;
    };
  }, [establishSession]);

  const completeAuthentication = useCallback(
    async (
      response: AuthSession.AuthSessionResult | null,
      request: AuthSession.AuthRequest | null
    ) => {
      if (!response || !request) {
        return;
      }

      if (response.type === 'cancel' || response.type === 'dismiss') {
        setIsBusy(false);
        return;
      }

      if (response.type === 'error') {
        setError(response.error?.description ?? 'Authentification refusée par Keycloak.');
        setIsBusy(false);
        return;
      }

      if (response.type !== 'success') {
        return;
      }

      const code = response.params.code;
      if (!code || handledCodes.current.has(code)) {
        return;
      }
      handledCodes.current.add(code);

      try {
        setIsBusy(true);
        setError(null);
        if (!request.codeVerifier) {
          throw new Error('La vérification PKCE est introuvable.');
        }

        const tokens = await AuthSession.exchangeCodeAsync(
          {
            clientId,
            code,
            redirectUri,
            extraParams: { code_verifier: request.codeVerifier },
          },
          oidcDiscovery
        );
        await establishSession(toStoredSession(tokens));
      } catch (authenticationError) {
        setError(
          authenticationError instanceof Error
            ? authenticationError.message
            : 'Connexion impossible.'
        );
      } finally {
        setIsBusy(false);
      }
    },
    [establishSession, redirectUri]
  );

  const login = useCallback(async () => {
    setError(null);
    setIsBusy(true);
    try {
      const response = await promptLogin();
      await completeAuthentication(response, loginRequest);
    } catch {
      setError("Le service d'authentification n'est pas disponible.");
      setIsBusy(false);
    }
  }, [completeAuthentication, loginRequest, promptLogin]);

  const registerAccount = useCallback(async () => {
    setError(null);
    setIsBusy(true);
    try {
      const response = await promptRegistration();
      await completeAuthentication(response, registerRequest);
    } catch {
      setError("Le service d'inscription n'est pas disponible.");
      setIsBusy(false);
    }
  }, [completeAuthentication, promptRegistration, registerRequest]);

  const logout = useCallback(async () => {
    const refreshToken = session?.refreshToken;
    setSession(null);
    setUser(null);
    setError(null);
    await clearStoredSession();

    if (refreshToken && oidcDiscovery.endSessionEndpoint) {
      try {
        await fetch(oidcDiscovery.endSessionEndpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: new URLSearchParams({ client_id: clientId, refresh_token: refreshToken }).toString(),
        });
      } catch {
        // La session locale est déjà supprimée ; Keycloak pourra expirer la session distante.
      }
    }
  }, [session?.refreshToken]);

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken: session?.accessToken ?? null,
      canAuthenticate: Boolean(loginRequest && registerRequest),
      error,
      isBusy,
      isReady,
      login,
      logout,
      registerAccount,
      user,
    }),
    [
      error,
      isBusy,
      isReady,
      login,
      loginRequest,
      logout,
      registerAccount,
      registerRequest,
      session?.accessToken,
      user,
    ]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth doit être utilisé dans AuthProvider.');
  }
  return context;
}
