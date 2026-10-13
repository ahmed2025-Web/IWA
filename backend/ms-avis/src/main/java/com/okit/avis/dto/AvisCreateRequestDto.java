package com.okit.avis.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvisCreateRequestDto(
    @NotNull(message = "L'identifiant du produit est obligatoire")
    @JsonProperty("id_produit")
    @JsonAlias("idProduit")
    Long idProduit,

    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    @JsonProperty("id_utilisateur")
    @JsonAlias("idUtilisateur")
    Long idUtilisateur,

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note doit être au minimum de 1")
    @Max(value = 5, message = "La note doit être au maximum de 5")
    @JsonProperty("note")
    Integer note,

    @NotBlank(message = "Le commentaire est obligatoire")
    @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
    @JsonProperty("commentaire")
    String commentaire
) {}
