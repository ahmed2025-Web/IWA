# ADR 0006 Accès acheteur au service utilisateur

## Statut

Acceptée.

## Contexte

`user-service` conserve le profil métier utilisé par l'application acheteur.
L'isolation par le claim `sub` empêche un utilisateur de choisir l'identifiant
d'un autre profil, mais elle ne distingue pas à elle seule les différents
types de comptes Keycloak.

O'kit utilise le rôle realm `BUYER` pour identifier les comptes autorisés à
utiliser l'application acheteur. Les comptes créés par l'inscription publique
reçoivent ce rôle. Aucune donnée vendeur n'est stockée dans `user-service`.

## Décision

- toutes les routes `/api/users/**` exigent un JWT valide portant `BUYER` ;
- le rôle est lu depuis `realm_access.roles` et converti en autorité Spring
  `ROLE_BUYER` ;
- un token absent ou invalide produit `401 Unauthorized` ;
- un token valide sans `BUYER` produit `403 Forbidden` ;
- les routes de santé et de métriques restent accessibles sans JWT, car le
  Service Kubernetes n'est pas exposé directement par l'Ingress ;
- toute autre route est refusée par défaut ;
- l'application mobile vérifie aussi le rôle pour afficher une erreur claire,
  sans remplacer le contrôle obligatoire côté backend.

## Alternatives envisagées

### Accepter tout compte authentifié

Écartée : un compte technique ou destiné à une autre interface ne doit pas
pouvoir créer un profil acheteur uniquement parce qu'il possède un token.

### Créer un second microservice utilisateur pour les vendeurs

Écartée : aucun profil métier vendeur n'est requis. Keycloak reste propriétaire
de leur identité et les futurs services métier appliqueront leurs propres
autorisations.

### Faire confiance uniquement au contrôle du client mobile

Écartée : un client peut être contourné. Le service propriétaire des données
doit toujours prendre la décision finale.

## Conséquences

- le realm Keycloak doit créer et inclure `BUYER` dans les tokens du client
  `okit-mobile` ;
- les tests couvrent les réponses `401`, `403` et le succès avec `BUYER` ;
- l'ajout d'un nouveau type de compte ne donne aucun accès implicite au profil
  acheteur ;
- aucune table, migration ou API vendeur n'est créée dans `user-service`.
