package com.okit.avis.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record AvisResponseDto(
    @JsonProperty("id_avis")
    @JsonAlias("idAvis")
    Long idAvis,

    @JsonProperty("id_produit")
    @JsonAlias("idProduit")
    Long idProduit,

    @JsonProperty("id_utilisateur")
    @JsonAlias("idUtilisateur")
    Long idUtilisateur,

    @JsonProperty("note")
    Integer note,

    @JsonProperty("commentaire")
    String commentaire,

    @JsonProperty("date_publication")
    @JsonAlias("datePublication")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate datePublication
) {}
