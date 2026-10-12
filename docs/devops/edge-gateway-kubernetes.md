# Gateway d'entrée Kubernetes

## Responsabilité

La gateway de la première tranche est la ressource Ingress située dans
`infra/kubernetes/gateway`. Elle s'appuie sur Traefik, déjà géré par le cluster,
et ne nécessite aucun conteneur supplémentaire dans le namespace.

Routes publiques :

| Préfixe | Destination interne | Protection |
| --- | --- | --- |
| `/api/users` | `user-service:http` | JWT vérifié par `user-service` |
| `/realms` | `keycloak:http` | parcours OIDC public |
| `/resources` | `keycloak:http` | ressources de l'interface OIDC |

La console Keycloak, PostgreSQL, `/health` et `/metrics` restent internes.

## Coexistence avec le nginx initial

L'Ingress fourni au groupe route actuellement `/` vers nginx. Les préfixes de
la gateway O'kit sont plus spécifiques et peuvent donc être ajoutés sans
supprimer immédiatement la page initiale.

Lorsque le frontend React disposera de son Service Kubernetes, sa route `/`
sera ajoutée à la gateway O'kit. Le Deployment, le Service et l'Ingress nginx
pourront alors être retirés de manière coordonnée.

## TLS et CORS

L'Ingress réutilise l'hôte, l'issuer cert-manager et le Secret TLS déjà fournis.
La production étant servie sur un hôte unique, aucun CORS permissif n'est
nécessaire. Une origine locale ne devra être ajoutée qu'avec une valeur précise,
jamais avec `*` pour des requêtes authentifiées.
