# O-Kit

## Authentification locale

La configuration Keycloak et ses procédures de lancement sont décrites dans [`docs/auth/keycloak.md`](docs/auth/keycloak.md). Les manifests Kubernetes se trouvent dans [`infra/keycloak/`](infra/keycloak/README.md).

## Services

- [`user-service`](services/user-service/README.md) : profil métier de l'acheteur authentifié.

Les manifests Kubernetes du service utilisateur sont documentés dans [`infra/kubernetes/user-service/`](infra/kubernetes/user-service/README.md).

La gateway d'entrée et les routes publiques sont documentées dans [`infra/kubernetes/gateway/`](infra/kubernetes/gateway/README.md).
