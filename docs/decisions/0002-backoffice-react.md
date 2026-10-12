# ADR 0002 Backoffice web en React

## Statut

Acceptée.

## Contexte

Les documents fonctionnels historiques se contredisent sur la technologie du backoffice commerçant : certains indiquent React, d'autres Angular. Cette contradiction bloque les choix de structure, de composants et d'intégration avec l'authentification.

## Décision

Le backoffice web O'kit sera développé avec React.

Cette décision ne lance pas l'implémentation du backoffice dans la présente étape. Elle fixe uniquement la technologie afin que les contrats API, les clients OAuth2 et les futurs choix d'architecture restent cohérents.

## Alternatives envisagées

### Angular

Angular figurait dans certains documents de use cases, mais n'est plus retenu à la suite de la décision explicite du porteur du projet.

## Conséquences

- les références à Angular dans les documents historiques sont considérées comme obsolètes ;
- le futur client Keycloak du backoffice sera un client public adapté à une SPA React ;
- les règles CORS du gateway devront autoriser uniquement les origines réellement utilisées par le backoffice ;
- aucune migration Angular vers React n'est nécessaire, car le backoffice n'est pas encore implémenté.
