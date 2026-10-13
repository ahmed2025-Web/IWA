package com.okit.avis.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductRatingDto(
    @JsonProperty("id_produit")
    @JsonAlias("idProduit")
    Long idProduit,

    @JsonProperty("note_moyenne")
    @JsonAlias("noteMoyenne")
    Double noteMoyenne,

    @JsonProperty("total_avis")
    @JsonAlias("totalAvis")
    Long totalAvis
) {}
