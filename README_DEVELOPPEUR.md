# O'Kit — Guide développeur

## 1. Prérequis

Installer :
- Git
- Docker et Docker Compose
- Node.js et npm

## 2. Cloner le projet

```bash
git clone git@gitlab.com:iwa-group2/o-kit.git
cd o-kit
git switch feature/auth-login
```

## 3. Configurer les variables d'environnement

À la racine du projet :

```bash
cp .env.example .env
nano .env
```

Remplacer les mots de passe par des valeurs personnelles :

```env
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=mot_de_passe_admin
USER_DB_PASSWORD=mot_de_passe_postgres
```

**Ne jamais commiter le fichier `.env`.**

## 4. Démarrer les services Docker

```bash
docker compose up --build -d
docker compose ps
```

Attendre le démarrage des quatre services :
- Keycloak
- PostgreSQL
- User-service
- API Gateway

## 5. Configurer Keycloak

Depuis la racine du projet :

```bash
set -a
source .env
set +a

bash infra/keycloak/reconcile-local-realm.sh
```

Cette commande configure le realm `okit`, les clients et les rôles `BUYER` et `SELLER`.

Administration Keycloak : http://localhost:8080/admin

Identifiant : `admin`

Mot de passe : celui défini dans `.env`.

## 6. Lancer l'application acheteur

```bash
cd apps/mobile-client

cp .env.example .env.local
npm ci
npm run web
```

Ouvrir : http://localhost:8081

**Important :** utiliser le port `8081`, autorisé dans les redirections Keycloak.

## 7. Tester l'inscription et la connexion

Depuis la racine du projet :

```bash
bash services/user-service/scripts/test-local-user-flow.sh
```

Résultat attendu :

```text
Local user flow succeeded: registration, PKCE login, profile persistence, reconnection and token refresh.
```

Le test vérifie :
- L'inscription d'un acheteur dans Keycloak
- L'attribution du rôle `BUYER`
- L'authentification avec PKCE
- La création du profil dans PostgreSQL
- La déconnexion et la reconnexion
- Le renouvellement du token

Tester également manuellement la création d'un compte depuis l'application Expo.

## 8. Adresses locales

| Service | URL |
|---|---|
| Application Expo | http://localhost:8081 |
| Keycloak | http://localhost:8080 |
| Administration Keycloak | http://localhost:8080/admin |
| API Gateway | http://localhost:8083 |
| User-service | http://localhost:8082 |

## 9. Commandes utiles

```bash
# Vérifier l'état des conteneurs
docker compose ps

# Consulter les logs
docker compose logs -f

# Reconstruire et redémarrer les services
docker compose up --build -d

# Arrêter les services sans supprimer les données
docker compose down

# Récupérer les dernières modifications
git pull
```

**Attention : ne pas utiliser `docker compose down -v`.**

Cette commande supprime les volumes Docker et entraîne la perte des comptes Keycloak et des profils PostgreSQL locaux.

## 10. Informations importantes

- Chaque développeur utilise son propre fichier `.env`.
- Les comptes et mots de passe utilisateurs sont gérés uniquement par Keycloak.
- PostgreSQL O'Kit stocke uniquement les profils métier.
- Les données Keycloak sont conservées dans un volume Docker.
- Au premier démarrage, Keycloak importe automatiquement le realm `okit`.
- Ne jamais commiter de mots de passe, tokens ou fichiers `.env`.
