# Parcours d'authentification du backoffice vendeur

## Objectif

Le backoffice React permet à un employé O'kit autorisé de se connecter avec un
compte créé par un administrateur. Cette tranche fournit l'authentification, le
contrôle du rôle et un écran d'accueil. Elle ne crée ni API ni microservice
catalogue.

## Sources de vérité

Keycloak conserve l'identité, le mot de passe, les sessions et le rôle realm
`SELLER`. `user-service` ne conserve aucune donnée vendeur.

## Accès autorisés

| Interface | Rôle obligatoire | Rôle entraînant un refus |
| --- | --- | --- |
| Application acheteur | `BUYER` | `SELLER` |
| Backoffice React | `SELLER` | `BUYER` |

Un compte portant simultanément les deux rôles est refusé par les deux
interfaces et par l'API de profil acheteur.

## Gestion administrative des vendeurs

Il n'existe aucune inscription publique donnant le rôle `SELLER`. Depuis la
console d'administration Keycloak, un administrateur :

1. crée l'utilisateur dans le realm `okit` ;
2. définit un mot de passe temporaire ou l'action `UPDATE_PASSWORD` ;
3. retire le mapping `default-roles-okit`, qui fournit `BUYER` par héritage ;
4. attribue le rôle realm `SELLER` ;
5. vérifie que les rôles effectifs ne contiennent plus `BUYER` ;
6. désactive ou supprime le compte lors du départ de l'employé.

Le mot de passe vendeur n'est jamais ajouté au dépôt. L'inscription publique
du realm reste disponible pour les acheteurs, mais elle n'accorde jamais
`SELLER` et ne permet donc pas d'entrer dans le backoffice.

En développement local, la même opération peut être exécutée de manière
reproductible par un administrateur :

```bash
set -a
source .env
set +a
SELLER_EMAIL='vendeur@example.test' \
SELLER_TEMPORARY_PASSWORD='mot-de-passe-temporaire' \
SELLER_FIRST_NAME='Camille' \
SELLER_LAST_NAME='Martin' \
./infra/keycloak/create-local-seller.sh
```

Le script refuse d'écraser un compte existant et ne journalise jamais le mot de
passe. Par défaut, Keycloak demande au vendeur de remplacer ce mot de passe à
la première connexion.

## Client web

Le client public `okit-seller-web` utilise :

- Authorization Code avec PKCE S256 ;
- `http://localhost:5173/*` en développement ;
- le domaine HTTPS O'kit sous `/backoffice/*` pour la cible déployée ;
- les scopes `openid profile email roles` ;
- `keycloak-js`, avec tokens en mémoire uniquement et renouvellement avant
  expiration.

## Critères d'acceptation

- un compte exclusivement `SELLER` ouvre le backoffice ;
- un compte `BUYER` est refusé par le backoffice ;
- un vendeur est refusé par l'application et l'API de profil acheteur ;
- un compte portant les deux rôles est refusé partout ;
- une inscription publique ne peut pas obtenir `SELLER` ;
- aucune donnée vendeur n'est créée dans PostgreSQL.
