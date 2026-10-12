# Gateway d'entrée O'kit

Cette ressource Ingress cible le namespace `polytech-dams5-07` et centralise
les routes publiques actuellement livrées par O'kit.

## Prérequis

- Keycloak déployé avec le Service interne `keycloak` ;
- `user-service` déployé avec son Service interne ;
- IngressClass `traefik` disponible ;
- certificat du domaine du groupe disponible dans le Secret `nginx-tls`.

## Validation et déploiement

```bash
kubectl kustomize infra/kubernetes/gateway
kubectl apply --dry-run=server -k infra/kubernetes/gateway
kubectl apply -k infra/kubernetes/gateway
```

Le nginx initial peut continuer à traiter `/`. Ne le supprimez pas avant que le
frontend React possède son propre Deployment et son propre Service.

## Contrôles

```bash
curl --fail https://dams5-07.students.polytech.lange.xyz/realms/okit/.well-known/openid-configuration

# L'API est joignable mais reste protégée : sans JWT, le statut attendu est 401.
curl --output /dev/null --write-out '%{http_code}\n' \
  https://dams5-07.students.polytech.lange.xyz/api/users/me
```

La décision d'architecture et la matrice des routes sont décrites dans
[`docs/devops/edge-gateway-kubernetes.md`](../../../docs/devops/edge-gateway-kubernetes.md).
