# ADR 0003 Frontière du service utilisateur

## Statut

Acceptée. Le périmètre décrit dans cette décision constitue la frontière
complète de `user-service`.

## Contexte

L'application mobile doit permettre à un acheteur authentifié de consulter et modifier ses informations personnelles. Keycloak est déjà retenu comme fournisseur d'identité et gère les identifiants, les mots de passe, les sessions et la réinitialisation du mot de passe.

Dupliquer les mots de passe ou administrer les comptes Keycloak depuis une base métier créerait deux sources de vérité et augmenterait les risques de sécurité.

## Décision

Un microservice Spring Boot nommé `user-service` est propriétaire du profil métier du client.

Pour sa première tranche, il conserve :

- l'identifiant technique du profil ;
- le sujet Keycloak issu du claim JWT `sub` ;
- le prénom ;
- le nom ;
- le téléphone ;
- la date de naissance facultative ;
- les dates de création et de mise à jour.

Il ne conserve pas :

- le mot de passe ;
- les tokens ;
- les sessions ;
- les rôles Keycloak ;
- l'adresse e-mail comme donnée métier propriétaire.

L'e-mail retourné par l'API provient du JWT courant. Le service ne permet d'accéder qu'au profil associé au `sub` du token authentifié.

Les adresses, préférences et wishlist ne font pas partie de `user-service`.
Elles seront prises en charge par d'autres domaines métier lorsqu'ils seront
définis. Elles ne doivent donc pas entraîner l'ajout de tables ou d'endpoints
dans ce service.

## API initiale

- `GET /api/users/me` consulte le profil courant ;
- `PUT /api/users/me` crée ou remplace de façon idempotente le profil courant.

Ces routes exigent un JWT valide portant le rôle realm `BUYER`. Cette règle
d'autorisation, ajoutée après la définition initiale du MVP, est détaillée dans
l'[ADR 0006](0006-buyer-access-to-user-service.md). L'isolation par `sub`
continue de garantir qu'un acheteur agit uniquement sur son propre profil.

## Alternatives envisagées

### Stocker les identifiants dans le service utilisateur

Écartée, car Keycloak est la source de vérité de l'identité et des mots de passe.

### Utiliser directement Keycloak comme base de profil métier

Écartée, car le profil métier utile à O'Kit ne doit pas coupler le domaine
client au modèle interne du fournisseur d'identité.

### Exposer un identifiant utilisateur dans l'URL

Écartée pour le profil personnel. L'endpoint `/me` réduit le risque qu'un client tente d'accéder au profil d'un autre utilisateur.

## Conséquences

- chaque requête métier est authentifiée comme OAuth2 Resource Server et
  exige le rôle `BUYER` ;
- `sub` est une clé externe immuable et unique ;
- le service possède sa table et ses migrations ;
- un changement d'e-mail reste du ressort de Keycloak ;
- les adresses, préférences, wishlist et données des autres domaines ne sont
  pas des évolutions attendues de `user-service` ;
- les futurs endpoints administratifs nécessiteront une décision explicite sur les rôles.
