# Préparation du parcours utilisateur sur Kubernetes

## Périmètre

Cet overlay déploie uniquement le parcours utilisateur :

- Keycloak et sa base PostgreSQL dédiée ;
- `user-service` et sa base PostgreSQL dédiée ;
- `api-gateway` ;
- les routes Ingress `/api`, `/realms` et `/resources` ;
- les `ServiceMonitor` de ces trois composants.

Il ne contient ni catalogue ni backoffice. Les ressources historiques `nginx`,
`okit-backend`, `postgres-db`, `pgdata-pvc` et le Secret TLS `nginx-tls` ne sont
ni modifiés ni supprimés.

## Images

L'overlay `infra/kubernetes/overlays/user-staging` utilise les chemins complets :

- `registry.gitlab.com/iwa-group2/o-kit/keycloak:a08da78` ;
- `registry.gitlab.com/iwa-group2/o-kit/user-service:a08da78` ;
- `registry.gitlab.com/iwa-group2/o-kit/api-gateway:a08da78`.

Le tag `a08da78` correspond au commit applicatif validé
`feat(user): harden buyer profile service`. Il est immuable et remplace tout
tag flottant ; le tag `latest` est interdit. Une future version devra mettre à
jour ensemble les trois références après validation du nouveau SHA.

Les Deployments utilisent le Secret `gitlab-registry` via `imagePullSecrets`.
Ce Secret est créé hors Git et ses valeurs ne doivent jamais apparaître dans
les logs ou les manifests versionnés.

## Bases et persistance

Les bases `keycloak-postgres` et `user-postgres` possèdent chacune leur propre
PVC de 2 Gio sur la classe `nfs-tower`. Elles ne réutilisent pas `postgres-db`
ni `pgdata-pvc`, qui appartiennent au backend historique et doivent être
préservés intégralement.

Les Secrets requis sont :

- `keycloak-secrets` avec `database-password`, `bootstrap-admin-username` et
  `bootstrap-admin-password` ;
- `user-service-secrets` avec `database-password` ;
- `gitlab-registry` avec les credentials de lecture du registre.

Les valeurs sont générées et appliquées directement au cluster, sans fichier
intermédiaire persistant et sans affichage dans les logs.

## Préparation locale des images

Depuis la racine du dépôt :

```bash
docker build --pull -t registry.gitlab.com/iwa-group2/o-kit/keycloak:a08da78 infra/keycloak
docker build --pull -t registry.gitlab.com/iwa-group2/o-kit/user-service:a08da78 services/user-service
docker build --pull -t registry.gitlab.com/iwa-group2/o-kit/api-gateway:a08da78 services/api-gateway

docker push registry.gitlab.com/iwa-group2/o-kit/keycloak:a08da78
docker push registry.gitlab.com/iwa-group2/o-kit/user-service:a08da78
docker push registry.gitlab.com/iwa-group2/o-kit/api-gateway:a08da78
```

La publication des images ne modifie pas Kubernetes.

## Prévisualisation sans déploiement

Rendre l'overlay versionné sans le modifier :

```bash
kubectl kustomize infra/kubernetes/overlays/user-staging > /tmp/okit-user-staging.yaml
kubectl apply --dry-run=server -f /tmp/okit-user-staging.yaml
kubectl diff -f /tmp/okit-user-staging.yaml
```

`kubectl diff` peut retourner le code `1` lorsqu'il existe des différences
attendues. Aucune commande `kubectl apply` réelle ne doit être exécutée sans
confirmation explicite.

## Ordre de déploiement prévu

Après validation du diff :

1. créer directement dans Kubernetes les Secrets manquants ;
2. appliquer PostgreSQL Keycloak et attendre sa disponibilité ;
3. appliquer Keycloak et vérifier son rollout, son issuer OIDC et ses métriques ;
4. appliquer PostgreSQL utilisateur et attendre sa disponibilité ;
5. appliquer `user-service`, vérifier Flyway, les probes et les métriques ;
6. appliquer `api-gateway`, puis l'Ingress ;
7. vérifier `/realms`, `/resources`, le `401` sans JWT, le succès avec un JWT
   `BUYER`, le refus sans ce rôle et les cibles Prometheus.

Un retour arrière restaure uniquement les workloads applicatifs ou retire les
routes ajoutées. Aucun PVC ne doit être supprimé.
