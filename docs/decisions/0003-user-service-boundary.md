# ADR 0003 Frontière du service utilisateur

## Statut

Acceptée pour le MVP du profil client.

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

Les adresses et la wishlist appartiennent fonctionnellement au domaine client, mais seront ajoutées dans des tranches ultérieures avec leurs propres contrats et migrations.

## API initiale

- `GET /api/users/me` consulte le profil courant ;
- `PUT /api/users/me` crée ou remplace de façon idempotente le profil courant.

Ces routes exigent un JWT valide. Aucun rôle métier n'est requis dans cette première tranche : la possession d'un token valide et l'isolation par `sub` suffisent pour agir sur son propre profil.

## Alternatives envisagées

### Stocker les identifiants dans le service utilisateur

Écartée, car Keycloak est la source de vérité de l'identité et des mots de passe.

### Utiliser directement Keycloak comme base de profil métier

Écartée, car les données métier amenées à évoluer, comme les adresses et la wishlist, ne doivent pas coupler le domaine client au modèle interne du fournisseur d'identité.

### Exposer un identifiant utilisateur dans l'URL

Écartée pour le profil personnel. L'endpoint `/me` réduit le risque qu'un client tente d'accéder au profil d'un autre utilisateur.

## Conséquences

- chaque requête métier est authentifiée comme OAuth2 Resource Server ;
- `sub` est une clé externe immuable et unique ;
- le service possède sa table et ses migrations ;
- un changement d'e-mail reste du ressort de Keycloak ;
- les futurs endpoints administratifs nécessiteront une décision explicite sur les rôles.
