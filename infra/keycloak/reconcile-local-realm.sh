#!/bin/sh

set -eu

: "${KEYCLOAK_ADMIN_PASSWORD:?Define KEYCLOAK_ADMIN_PASSWORD in the environment}"

KEYCLOAK_ADMIN_USERNAME=${KEYCLOAK_ADMIN_USERNAME:-admin}
SCRIPT_DIRECTORY=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPOSITORY_ROOT=$(CDPATH= cd -- "$SCRIPT_DIRECTORY/../.." && pwd)

export OKIT_KEYCLOAK_ADMIN_USERNAME=$KEYCLOAK_ADMIN_USERNAME
export OKIT_KEYCLOAK_ADMIN_PASSWORD=$KEYCLOAK_ADMIN_PASSWORD

cd "$REPOSITORY_ROOT"

docker compose exec -T \
  -e OKIT_KEYCLOAK_ADMIN_USERNAME \
  -e OKIT_KEYCLOAK_ADMIN_PASSWORD \
  keycloak sh -s <<'KEYCLOAK_SCRIPT'
set -eu

KCADM=/opt/keycloak/bin/kcadm.sh
CONFIG=$(mktemp)
SERVER_URL=http://127.0.0.1:8080
REALM=okit
CLIENT_ID=okit-mobile
BUYER_ROLE=BUYER

cleanup() {
  rm -f "$CONFIG"
}
trap cleanup EXIT

kcadm() {
  command=$1
  shift
  "$KCADM" "$command" --config "$CONFIG" "$@"
}

"$KCADM" config credentials \
  --config "$CONFIG" \
  --server "$SERVER_URL" \
  --realm master \
  --user "$OKIT_KEYCLOAK_ADMIN_USERNAME" \
  --password "$OKIT_KEYCLOAK_ADMIN_PASSWORD" >/dev/null

if ! kcadm get "realms/$REALM" >/dev/null 2>&1; then
  echo "Realm '$REALM' absent. Start from an empty volume to run the versioned import first." >&2
  exit 1
fi

kcadm update "realms/$REALM" \
  -s enabled=true \
  -s sslRequired=external \
  -s registrationAllowed=true \
  -s registrationEmailAsUsername=true \
  -s resetPasswordAllowed=true \
  -s rememberMe=true \
  -s loginWithEmailAllowed=true \
  -s duplicateEmailsAllowed=false \
  -s verifyEmail=false >/dev/null

if ! kcadm get "roles/$BUYER_ROLE" -r "$REALM" >/dev/null 2>&1; then
  kcadm create roles -r "$REALM" \
    -s "name=$BUYER_ROLE" \
    -s "description=Acheteur autorisé à utiliser l'application mobile O'kit" >/dev/null
fi

CLIENT_UUID=$(
  kcadm get clients -r "$REALM" \
    -q "clientId=$CLIENT_ID" \
    --fields id \
    --format csv \
    --noquotes
)

if [ -z "$CLIENT_UUID" ]; then
  kcadm create clients -r "$REALM" -s "clientId=$CLIENT_ID" >/dev/null
  CLIENT_UUID=$(
    kcadm get clients -r "$REALM" \
      -q "clientId=$CLIENT_ID" \
      --fields id \
      --format csv \
      --noquotes
  )
fi

kcadm update "clients/$CLIENT_UUID" -r "$REALM" \
  -s "clientId=$CLIENT_ID" \
  -s "name=O'kit mobile" \
  -s "description=Client public OIDC de l'application Expo O'kit" \
  -s enabled=true \
  -s protocol=openid-connect \
  -s publicClient=true \
  -s bearerOnly=false \
  -s standardFlowEnabled=true \
  -s implicitFlowEnabled=false \
  -s directAccessGrantsEnabled=false \
  -s serviceAccountsEnabled=false \
  -s fullScopeAllowed=false \
  -s 'redirectUris=["okit://auth","http://localhost:8081","http://localhost:8081/*","https://dams5-07.students.polytech.lange.xyz","https://dams5-07.students.polytech.lange.xyz/*"]' \
  -s 'webOrigins=["http://localhost:8081","https://dams5-07.students.polytech.lange.xyz"]' \
  -s 'attributes."pkce.code.challenge.method"=S256' \
  -s 'defaultClientScopes=["basic","profile","email","roles","web-origins"]' \
  -s 'optionalClientScopes=["address","phone","offline_access"]' >/dev/null

DEFAULT_ROLE="default-roles-$REALM"
if ! kcadm get-roles -r "$REALM" --rname "$DEFAULT_ROLE" \
  --fields name --format csv --noquotes | grep -Fx "$BUYER_ROLE" >/dev/null; then
  kcadm add-roles -r "$REALM" --rname "$DEFAULT_ROLE" --rolename "$BUYER_ROLE"
fi

if ! kcadm get "clients/$CLIENT_UUID/scope-mappings/realm" -r "$REALM" \
  --fields name --format csv --noquotes | grep -Fx "$BUYER_ROLE" >/dev/null; then
  BUYER_ROLE_ID=$(
    kcadm get "roles/$BUYER_ROLE" -r "$REALM" \
      --fields id --format csv --noquotes
  )
  kcadm create "clients/$CLIENT_UUID/scope-mappings/realm" -r "$REALM" \
    --body "[{\"id\":\"$BUYER_ROLE_ID\",\"name\":\"$BUYER_ROLE\"}]" >/dev/null
fi

kcadm get "roles/$BUYER_ROLE" -r "$REALM" --fields name >/dev/null
kcadm get "clients/$CLIENT_UUID" -r "$REALM" \
  --fields clientId,enabled,publicClient,standardFlowEnabled,directAccessGrantsEnabled,fullScopeAllowed >/dev/null

echo "Realm '$REALM', role '$BUYER_ROLE' and client '$CLIENT_ID' reconciled successfully."
KEYCLOAK_SCRIPT
