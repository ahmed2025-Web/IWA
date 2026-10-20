# Déploiement Kubernetes du service utilisateur

## Périmètre

Cette première tranche Kubernetes déploie :

- `user-service` comme Deployment sans état ;
- sa base PostgreSQL comme StatefulSet avec un volume persistant ;
- les Services internes correspondants ;
- les probes de démarrage, disponibilité et vivacité ;
- un `ServiceMonitor` pour collecter `/metrics` ;
- une NetworkPolicy limitant l'accès à PostgreSQL au service utilisateur.

Le namespace cible est `polytech-dams5-07`.

## Exposition réseau

`user-service` reste un Service interne. Traefik publie `/api/**` vers Spring
Cloud Gateway, qui route ensuite `/api/users/**` vers le service sur l'hôte du
cluster :

```text
https://dams5-07.students.polytech.lange.xyz
```

Cette séparation évite de multiplier les Ingress. Spring Cloud Gateway valide
le JWT et centralise le CORS, puis `user-service` valide à nouveau le token
avant d'accéder aux données métier.

## Secrets

Le mot de passe PostgreSQL n'est pas versionné. Un Secret nommé `user-service-secrets`, contenant la clé `database-password`, doit exister avant le déploiement.

L'image du service est publiée sous
`registry.gitlab.com/iwa-group2/o-kit/user-service:<sha-git>`. L'overlay conserve
un tag volontairement invalide jusqu'à la sélection du commit applicatif
canonique. Le Deployment utilise le Secret `gitlab-registry`.

## Ressources et stockage

Les ressources sont dimensionnées pour rester raisonnables dans le quota du namespace :

| Composant | CPU demandé | Mémoire demandée | CPU maximum | Mémoire maximum |
| --- | ---: | ---: | ---: | ---: |
| `user-service` | 150m | 256Mi | 500m | 512Mi |
| PostgreSQL | 100m | 256Mi | 500m | 512Mi |

PostgreSQL demande un PVC de 2 Gio. Le service utilisateur peut être redéployé sans perdre les profils ; la suppression volontaire du PVC reste une opération destructive distincte.

Le PVC utilise la classe `nfs-tower` et reste strictement séparé de
`pgdata-pvc`, propriété du backend historique déjà présent dans le namespace.

## Dépendances

Les manifests Keycloak sont fournis dans `infra/keycloak`. Ils créent le Service
interne `keycloak`, importent le realm `okit` et publient l'issuer HTTPS attendu.

La gateway applicative et l'Ingress sont fournis dans
`infra/kubernetes/gateway`. Traefik conserve l'hôte et le certificat du cluster
et route `/api/**` vers `api-gateway`, qui conserve le chemin
`/api/users/**` lors de l'appel au service.
