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

`user-service` n'est pas exposé directement par un Ingress. Le futur API Gateway sera l'unique point d'entrée public sur l'hôte fourni par le cluster :

```text
https://dams5-07.students.polytech.lange.xyz
```

Cette séparation évite de multiplier les Ingress et conserve le contrôle transversal de l'authentification, du routage et du CORS dans le gateway.

## Secrets

Le mot de passe PostgreSQL n'est pas versionné. Un Secret nommé `user-service-secrets`, contenant la clé `database-password`, doit exister avant le déploiement.

L'image du service doit également être publiée par la chaîne CI dans le registre retenu par l'équipe. Le manifeste utilise une référence volontairement invalide tant que ce registre et le tag traçable ne sont pas fournis.

## Ressources et stockage

Les ressources sont dimensionnées pour rester raisonnables dans le quota du namespace :

| Composant | CPU demandé | Mémoire demandée | CPU maximum | Mémoire maximum |
| --- | ---: | ---: | ---: | ---: |
| `user-service` | 150m | 256Mi | 500m | 512Mi |
| PostgreSQL | 100m | 256Mi | 500m | 512Mi |

PostgreSQL demande un PVC de 2 Gio. Le service utilisateur peut être redéployé sans perdre les profils ; la suppression volontaire du PVC reste une opération destructive distincte.

## Dépendances

Les manifests Keycloak sont fournis dans `infra/keycloak`. Ils créent le Service
interne `keycloak`, importent le realm `okit` et publient l'issuer HTTPS attendu.

Les dépendances restantes pour le parcours public complet sont :

- l'API Gateway et son Ingress conservant l'hôte et le certificat du cluster.
