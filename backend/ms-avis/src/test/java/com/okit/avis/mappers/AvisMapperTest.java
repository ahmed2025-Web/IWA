package com.okit.avis.mappers;

import com.okit.avis.dto.AvisCreateRequestDto;
import com.okit.avis.dto.AvisResponseDto;
import com.okit.avis.entities.Avis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AvisMapperTest {

    private final AvisMapper avisMapper = new AvisMapper();

    @Test
    @DisplayName("toDto convertit correctement une entité Avis en AvisResponseDto")
    void toDto_ShouldMapEntityToDto() {
        LocalDate date = LocalDate.of(2026, 10, 13);
        Avis avis = new Avis(1L, 100L, 200L, 5, "Excellent !", date);

        AvisResponseDto dto = avisMapper.toDto(avis);

        assertThat(dto).isNotNull();
        assertThat(dto.idAvis()).isEqualTo(1L);
        assertThat(dto.idProduit()).isEqualTo(100L);
        assertThat(dto.idUtilisateur()).isEqualTo(200L);
        assertThat(dto.note()).isEqualTo(5);
        assertThat(dto.commentaire()).isEqualTo("Excellent !");
        assertThat(dto.datePublication()).isEqualTo(date);
    }

    @Test
    @DisplayName("toDto avec entité nulle retourne null")
    void toDto_WhenNull_ShouldReturnNull() {
        assertThat(avisMapper.toDto(null)).isNull();
    }

    @Test
    @DisplayName("toEntity convertit un AvisCreateRequestDto en entité Avis")
    void toEntity_ShouldMapDtoToEntity() {
        AvisCreateRequestDto dto = new AvisCreateRequestDto(100L, 200L, 4, "Bon produit");

        Avis avis = avisMapper.toEntity(dto);

        assertThat(avis).isNotNull();
        assertThat(avis.getIdProduit()).isEqualTo(100L);
        assertThat(avis.getIdUtilisateur()).isEqualTo(200L);
        assertThat(avis.getNote()).isEqualTo(4);
        assertThat(avis.getCommentaire()).isEqualTo("Bon produit");
        assertThat(avis.getDatePublication()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("toEntity avec DTO nul retourne null")
    void toEntity_WhenNull_ShouldReturnNull() {
        assertThat(avisMapper.toEntity(null)).isNull();
    }
}
