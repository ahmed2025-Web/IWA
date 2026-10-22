# Modèle de données du service utilisateur

## Propriété des données

`user-service` est seul propriétaire de la table `user_profiles`. Aucun autre service ne doit lire ou modifier cette table directement.

Keycloak reste propriétaire de l'identité. Le champ `identity_subject` référence logiquement le claim `sub` du JWT, sans clé étrangère vers la base Keycloak.

## Table user_profiles

| Colonne | Type | Contraintes | Rôle |
| --- | --- | --- | --- |
| `id` | UUID | clé primaire | Identifiant métier technique |
| `identity_subject` | VARCHAR(255) | unique, non nul | Claim Keycloak `sub` |
| `first_name` | VARCHAR(100) | non nul | Prénom du profil |
| `last_name` | VARCHAR(100) | non nul | Nom du profil |
| `phone_number` | VARCHAR(32) | non nul | Téléphone |
| `birth_date` | DATE | facultatif, date passée ou présente | Date de naissance |
| `created_at` | TIMESTAMP WITH TIME ZONE | non nul | Création du profil |
| `updated_at` | TIMESTAMP WITH TIME ZONE | non nul | Dernière modification |
| `version` | BIGINT | non nul | Verrouillage optimiste des mises à jour concurrentes |

## Données volontairement absentes

- mot de passe et credentials ;
- access token et refresh token ;
- rôles et permissions ;
- e-mail du compte comme copie persistée ;
- données d'un autre domaine métier.

## Évolutions envisagées

La table `user_profiles` couvre le modèle complet du MVP actuel. Elle
n'anticipe ni les adresses de livraison ni la wishlist avec des colonnes
inutilisées.

Avant la tranche adresses, il faudra décider son service propriétaire, ses
cardinalités et ses règles de partage avec le domaine commande. La wishlist ne
sera modélisée qu'après stabilisation des identifiants et du contrat de
consultation du catalogue. Si ces fonctionnalités appartiennent finalement à
d'autres services, aucune table correspondante ne sera ajoutée à cette base.
