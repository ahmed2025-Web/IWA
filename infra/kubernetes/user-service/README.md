# user-service sur Kubernetes

Ces manifests ciblent exclusivement le namespace `polytech-dams5-07`. Ils ne créent ni Ingress ni Service public : le trafic externe passera par l'API Gateway.

## Prérequis

- une image `user-service` publiée dans le registre retenu par l'équipe ;
- un kubeconfig conservé hors du dépôt ;
- le Secret `user-service-secrets` ;
- les CRD Prometheus Operator pour le `ServiceMonitor`.

## Configuration obligatoire

Remplacer dans `kustomization.yaml` :

```yaml
images:
  - name: user-service
    newName: registre-retenu/okit/user-service
    newTag: tag-immuable
```

Le registre et le tag ne sont pas choisis ici, car leur gestion appartient à la chaîne CI de l'équipe.

## Création du Secret

Saisir le mot de passe sans l'écrire dans le dépôt :

```bash
read -rs OKIT_USER_DB_PASSWORD
kubectl -n polytech-dams5-07 create secret generic user-service-secrets \
  --from-literal=database-password="${OKIT_USER_DB_PASSWORD}"
unset OKIT_USER_DB_PASSWORD
```

Si le Secret existe déjà, sa rotation doit être coordonnée avec PostgreSQL ; le recréer avec une nouvelle valeur ne change pas automatiquement le mot de passe d'une base déjà initialisée.

## Validation et déploiement

```bash
kubectl kustomize infra/kubernetes/user-service
kubectl apply --dry-run=server -k infra/kubernetes/user-service
kubectl apply -k infra/kubernetes/user-service
kubectl -n polytech-dams5-07 rollout status statefulset/user-postgres
kubectl -n polytech-dams5-07 rollout status deployment/user-service
```

## Contrôles

```bash
kubectl -n polytech-dams5-07 get pods,services,pvc
kubectl -n polytech-dams5-07 port-forward service/user-service 8082:80
curl --fail http://127.0.0.1:8082/health
curl --fail http://127.0.0.1:8082/metrics
```

Un appel à `/api/users/me` sans JWT doit retourner `401`.

La conception et les dépendances restantes sont détaillées dans [`docs/devops/user-service-kubernetes.md`](../../../docs/devops/user-service-kubernetes.md).
