# Keycloak sur Kubernetes

Ces manifests ciblent exclusivement le namespace `polytech-dams5-07`. Ils
réutilisent le realm employé par Docker Compose afin de conserver une seule
configuration OIDC versionnée.

## Prérequis

- le kubeconfig du groupe, conservé hors du dépôt ;
- une image construite depuis `infra/keycloak/Dockerfile` et publiée dans le
  registre retenu par l'équipe ;
- le Secret `keycloak-secrets` ;
- les CRD Prometheus Operator pour le `ServiceMonitor` ;
- le certificat HTTPS de l'hôte fourni, référencé par l'Ingress.

Remplacer dans `kustomization.yaml` la valeur volontairement invalide
`registry.example.invalid/okit/keycloak:replace-me` par l'image immuable publiée
par la chaîne CI.

## Création initiale du Secret

Saisir les valeurs sans les écrire dans le dépôt ni dans l'historique du shell :

```bash
read -rp "Administrateur Keycloak : " OKIT_KEYCLOAK_ADMIN
read -rsp "Mot de passe administrateur : " OKIT_KEYCLOAK_ADMIN_PASSWORD
printf '\n'
read -rsp "Mot de passe PostgreSQL Keycloak : " OKIT_KEYCLOAK_DB_PASSWORD
printf '\n'

kubectl -n polytech-dams5-07 create secret generic keycloak-secrets \
  --from-literal=bootstrap-admin-username="${OKIT_KEYCLOAK_ADMIN}" \
  --from-literal=bootstrap-admin-password="${OKIT_KEYCLOAK_ADMIN_PASSWORD}" \
  --from-literal=database-password="${OKIT_KEYCLOAK_DB_PASSWORD}"

unset OKIT_KEYCLOAK_ADMIN OKIT_KEYCLOAK_ADMIN_PASSWORD OKIT_KEYCLOAK_DB_PASSWORD
```

La rotation du mot de passe de base doit être coordonnée avec PostgreSQL. Changer
uniquement le Secret ne modifie pas le mot de passe déjà enregistré dans une
base initialisée.

## Validation et déploiement

Depuis la racine du dépôt :

```bash
kubectl kustomize infra/keycloak
kubectl apply --dry-run=server -k infra/keycloak
kubectl apply -k infra/keycloak
kubectl -n polytech-dams5-07 rollout status statefulset/keycloak-postgres
kubectl -n polytech-dams5-07 rollout status deployment/keycloak
```

L'Ingress Keycloak peut coexister avec le nginx initial : ses chemins
`/realms` et `/resources` sont plus spécifiques que `/`. Le nginx ne sera
retiré que lorsque la gateway prendra en charge la route principale.

## Contrôles

```bash
kubectl -n polytech-dams5-07 get pods,services,ingress,pvc
kubectl -n polytech-dams5-07 port-forward service/keycloak 8080:8080
curl --fail http://127.0.0.1:8080/realms/okit/.well-known/openid-configuration
```

Pour vérifier la santé et les métriques sans les publier :

```bash
kubectl -n polytech-dams5-07 port-forward service/keycloak 9000:9000
curl --fail http://127.0.0.1:9000/health/ready
curl --fail http://127.0.0.1:9000/metrics
```

La conception, les limites de l'import et le budget de ressources sont décrits
dans [`docs/devops/keycloak-kubernetes.md`](../../docs/devops/keycloak-kubernetes.md).
