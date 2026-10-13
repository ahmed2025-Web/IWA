package com.okit.avis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AvisUpdateRequestDto(
    @Min(value = 1, message = "La note doit être au minimum de 1")
    @Max(value = 5, message = "La note doit être au maximum de 5")
    @JsonProperty("note")
    Integer note,

    @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
    @JsonProperty("commentaire")
    String commentaire
) {}
