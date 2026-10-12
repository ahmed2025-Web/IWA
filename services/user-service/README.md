# O'kit user-service

Ce service Spring Boot conserve le profil métier de l'acheteur authentifié. Keycloak reste propriétaire des identités, mots de passe, sessions et rôles.

## Configuration

| Variable | Description |
| --- | --- |
| `USER_DB_URL` | URL JDBC PostgreSQL |
| `USER_DB_USERNAME` | Utilisateur PostgreSQL |
| `USER_DB_PASSWORD` | Mot de passe PostgreSQL, obligatoire |
| `OIDC_ISSUER_URI` | Issuer du realm Keycloak |
| `OIDC_JWK_SET_URI` | Endpoint interne des clés publiques Keycloak |
| `SERVER_PORT` | Port HTTP, `8080` par défaut |

## Commandes

```bash
mvn test
mvn verify
mvn spring-boot:run
```

L'API est documentée dans `docs/api/user-service.openapi.yaml` à la racine du dépôt.

Depuis la racine du dépôt, l'environnement local complet démarre avec :

```bash
cp .env.example .env
docker compose up --build keycloak postgres user-service
```

Le service est alors exposé sur `http://localhost:8082`. Les endpoints de santé et de métriques sont respectivement `/health` et `/metrics`.

## Kubernetes

Les manifests et la procédure de déploiement sont décrits dans [`infra/kubernetes/user-service/README.md`](../../infra/kubernetes/user-service/README.md). Le service reste interne au cluster : l'exposition publique sera assurée par l'API Gateway et son Ingress.
