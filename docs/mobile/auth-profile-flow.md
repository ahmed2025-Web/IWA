# Parcours mobile/web d'authentification et de profil

## Objectif

Le client React Native avec aperçu Expo Web permet à un acheteur de :

1. se connecter à un compte existant ;
2. créer un compte dans Keycloak ;
3. revenir dans l'application avec Authorization Code et PKCE S256 ;
4. consulter son profil métier ;
5. compléter ou modifier son prénom, son nom, son téléphone et sa date de
   naissance ;
6. enregistrer ces informations dans `user-service`.

## Sources de vérité

Keycloak reste propriétaire :

- de l'identifiant de connexion ;
- de l'adresse e-mail ;
- du mot de passe ;
- des sessions et tokens.

`user-service` reste propriétaire :

- du lien avec le claim `sub` Keycloak ;
- du prénom et du nom utilisés par le domaine métier ;
- du téléphone ;
- de la date de naissance facultative.

L'e-mail affiché par l'API provient du JWT courant et n'est pas dupliqué dans la
table `user_profiles`.

## Comportement attendu

### Utilisateur non authentifié

- l'écran affiche « Se connecter » et « Créer un compte » ;
- les deux actions ouvrent l'interface Keycloak, jamais un formulaire de mot de
  passe propre à l'application ;
- le client OIDC utilisé est `okit-mobile` sans secret ;
- une erreur ou une annulation ramène à un état exploitable.

### Utilisateur authentifié

- le client appelle `GET /api/users/me` avec le bearer token ;
- une réponse `200` préremplit le formulaire depuis la base métier ;
- une réponse `404` affiche un formulaire vide, prérempli avec le prénom et le
  nom disponibles dans Keycloak ;
- l'enregistrement appelle `PUT /api/users/me` puis affiche les données
  retournées par le service ;
- une réponse `401` invalide la session locale et demande une reconnexion.

## Environnements

Les URL sont configurées avec des variables Expo publiques qui ne contiennent
aucun secret :

```text
EXPO_PUBLIC_KEYCLOAK_URL
EXPO_PUBLIC_API_URL
```

Valeurs locales par défaut :

```text
http://localhost:8080
http://localhost:8083
```

Pour l'aperçu web local, Spring Cloud Gateway autorise uniquement l'origine
`http://localhost:8081` et transmet les requêtes à `user-service`. En
Kubernetes, le client, la gateway et les APIs sont destinés à partager l'hôte
HTTPS du groupe.

## Limites de cette tranche

- la réinitialisation du mot de passe est proposée par Keycloak, mais l'envoi
  d'e-mail nécessite encore SMTP ;
- le renouvellement automatique d'une session expirée sera traité séparément ;
- aucun mot de passe ou token ne doit apparaître dans les journaux.
