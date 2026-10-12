# ADR 0005 Gateway d'entrée Kubernetes avec Traefik

## Statut

Acceptée pour la première tranche déployable d'O'kit.

## Contexte

Le cluster du groupe fournit Traefik comme contrôleur Ingress et impose une
exposition HTTPS par Ingress, sans `NodePort` ni `LoadBalancer`. Le projet doit
publier les parcours OIDC de Keycloak et l'API du `user-service` sur l'unique
hôte `dams5-07.students.polytech.lange.xyz`.

Aucun besoin documenté ne requiert actuellement une transformation de requête,
une agrégation de plusieurs réponses, une limitation de débit applicative ou
une authentification centralisée distincte de celle des microservices. Ajouter
un service Spring Cloud Gateway créerait donc une image, un déploiement, des
ressources et un point de panne supplémentaires sans responsabilité métier
justifiée.

## Décision

- Le contrôleur Traefik du cluster et une ressource Ingress O'kit constituent
  la gateway d'entrée de cette première tranche.
- L'Ingress conserve l'hôte et le certificat fournis au groupe.
- `/api/users` est routé vers le Service interne `user-service` sans réécriture
  de chemin.
- `/realms` et `/resources` sont routés vers le Service interne `keycloak`.
- La console Keycloak `/admin`, les bases de données, les probes et les
  métriques ne sont pas exposées publiquement.
- Le `user-service` reste responsable de la validation de ses JWT et de ses
  autorisations. La gateway ne remplace pas la sécurité applicative.
- Aucune règle CORS générique n'est ajoutée. Le backoffice de production sera
  servi sur le même hôte ; toute origine de développement supplémentaire devra
  être explicitement validée avant d'être autorisée.

## Alternatives envisagées

### Ajouter un microservice Spring Cloud Gateway

Écartée à cette étape faute de responsabilité supplémentaire justifiant son
coût opérationnel. Cette décision pourra être réévaluée si des besoins
d'agrégation, de résilience, de transformation ou de politiques transverses ne
peuvent pas être couverts proprement par l'Ingress.

### Créer un Ingress par service

Écartée afin de conserver un point de configuration unique pour l'hôte public,
le certificat et les routes exposées.

### Exposer les Services avec `NodePort` ou `LoadBalancer`

Écartée car interdite par les règles du cluster et inutile avec Traefik.

## Conséquences

- aucun pod ni budget CPU/mémoire supplémentaire n'est nécessaire pour la
  gateway ;
- les microservices restent responsables de leur sécurité métier ;
- les nouvelles routes publiques devront être ajoutées explicitement à
  l'Ingress central ;
- le nginx initial peut rester sur `/` pendant la construction du frontend,
  car les routes O'kit sont plus spécifiques ;
- lors de la livraison du frontend React, la route `/` sera transférée vers son
  Service puis le nginx initial pourra être supprimé.
