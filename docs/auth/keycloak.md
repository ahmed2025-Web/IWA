# Authentification locale avec Keycloak

Ce document décrit la configuration de développement de l'authentification O'kit. Keycloak porte les identités et les mots de passe. L'application mobile est un client public OIDC et utilise le flux Authorization Code avec PKCE S256.

Cette configuration est réservée au développement local. Le mode `start-dev`, HTTP et les URI localhost ne conviennent pas à la production.

## Configuration fournie

Le fichier `infra/keycloak/import/okit-realm.json` crée au premier démarrage :

- le realm `okit` ;
- le client public `okit-mobile` ;
- le flux Authorization Code ;
- l'obligation d'utiliser PKCE S256 ;
- l'inscription utilisateur ;
- la réinitialisation du mot de passe.

Le client ne possède pas de secret, car une application installée sur un appareil ne peut pas conserver un secret de manière fiable. Les flux implicite, Resource Owner Password Credentials et service account sont désactivés.

Les rôles métier ne sont pas définis à cette étape. Ils devront être ajoutés après validation du modèle d'autorisation.

## Prérequis

- Docker avec la commande `docker compose` ;
- le port local `8080` disponible ;
- une copie locale du fichier d'environnement d'exemple.

Créer le fichier local `.env` puis remplacer le mot de passe factice :

```bash
cp .env.example .env
```

Le fichier `.env` est ignoré par Git. Il ne doit jamais être ajouté au dépôt.

## Démarrage

Depuis la racine du dépôt :

```bash
docker compose up -d keycloak
```

La console d'administration est ensuite accessible à l'adresse `http://localhost:8080/admin/`. Le document de découverte OIDC du realm est exposé à l'adresse suivante :

```text
http://localhost:8080/realms/okit/.well-known/openid-configuration
```

Vérifier les journaux si le service ne démarre pas :

```bash
docker compose logs keycloak
```

Arrêter le service sans supprimer les données :

```bash
docker compose down
```

La commande suivante supprime aussi le volume et toutes les données locales de Keycloak. Elle permet de rejouer l'import initial après une modification du realm :

```bash
docker compose down --volumes
```

Cette suppression est destructive pour les comptes de développement créés localement.

## Paramètres du client mobile

| Paramètre | Valeur de développement |
| --- | --- |
| Realm | `okit` |
| Client | `okit-mobile` |
| Type | Client public |
| Flux | Authorization Code |
| PKCE | S256 obligatoire |
| URI native | `okit://auth` |
| URI web | `http://localhost:8081` |
| Scopes attendus | `openid profile email` |

L'étape d'implémentation mobile devra calculer l'URI de redirection avec Expo plutôt que la coder en dur. La valeur obtenue devra correspondre exactement à une URI autorisée dans Keycloak.

## Accès depuis Android ou un appareil physique

L'adresse `localhost:8080` fonctionne uniquement depuis la machine qui exécute Keycloak. Depuis un émulateur Android, l'hôte de développement est généralement accessible avec `10.0.2.2`. Depuis un appareil physique, il faut utiliser une adresse réseau ou un domaine de développement accessible par l'appareil.

Le port du conteneur est actuellement lié à `127.0.0.1` afin de ne pas exposer involontairement Keycloak sur le réseau local. Toute ouverture réseau doit être volontaire et documentée avant de modifier `compose.yaml`.

## Persistance et import

Le volume Docker `keycloak_data` conserve les utilisateurs et la configuration locale. Au démarrage, l'option `--import-realm` importe les fichiers JSON présents dans `/opt/keycloak/data/import` seulement si le realm n'existe pas déjà.

Une modification de `okit-realm.json` ne remplace donc pas automatiquement un realm déjà présent. Pour repartir de la configuration versionnée, il faut supprimer explicitement le volume local, en tenant compte de la perte des données de développement.

## Inscription et mot de passe oublié

L'inscription et la réinitialisation sont activées dans le realm. L'envoi effectif des courriels de réinitialisation nécessite encore une configuration SMTP. Aucun identifiant SMTP ne doit être inscrit dans le JSON du realm ou dans le dépôt.

## Limites connues

- aucune configuration de production n'est fournie ;
- aucun rôle métier n'est encore défini ;
- aucun serveur SMTP n'est configuré ;
- la validation de l'adresse e-mail est désactivée en développement ;
- le prototype mobile existant utilise encore `okit-web-dev` et une redirection codée en dur ; il sera adapté dans l'étape suivante.

## Références

- [Exécuter Keycloak dans un conteneur](https://www.keycloak.org/server/containers)
- [Importer et exporter des realms](https://www.keycloak.org/server/importExport)
- [Guide d'administration Keycloak](https://www.keycloak.org/docs/latest/server_admin/)
