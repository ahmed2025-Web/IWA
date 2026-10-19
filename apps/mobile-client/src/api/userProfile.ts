import { apiBaseUrl } from '../auth/config';

export type UserProfile = {
  id: string;
  subject: string;
  email: string | null;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  birthDate: string | null;
  createdAt: string;
  updatedAt: string;
};

export type UserProfileInput = Pick<
  UserProfile,
  'firstName' | 'lastName' | 'phoneNumber' | 'birthDate'
>;

export class UserProfileApiError extends Error {
  constructor(
    public readonly status: number,
    message: string
  ) {
    super(message);
  }
}

async function parseProfileResponse(response: Response): Promise<UserProfile> {
  if (!response.ok) {
    throw new UserProfileApiError(
      response.status,
      response.status === 401
        ? 'Ta session a expiré. Reconnecte-toi.'
        : `Le profil n'a pas pu être enregistré (${response.status}).`
    );
  }
  return response.json() as Promise<UserProfile>;
}

export async function getCurrentUserProfile(accessToken: string) {
  const response = await fetch(`${apiBaseUrl}/api/users/me`, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });

  if (response.status === 404) {
    return null;
  }
  return parseProfileResponse(response);
}

export async function saveCurrentUserProfile(
  accessToken: string,
  profile: UserProfileInput
) {
  const response = await fetch(`${apiBaseUrl}/api/users/me`, {
    method: 'PUT',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(profile),
  });

  return parseProfileResponse(response);
}
