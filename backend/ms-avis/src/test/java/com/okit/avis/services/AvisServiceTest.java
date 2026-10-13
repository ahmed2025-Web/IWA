package com.okit.avis.services;

import com.okit.avis.dto.AvisCreateRequestDto;
import com.okit.avis.dto.AvisResponseDto;
import com.okit.avis.dto.AvisUpdateRequestDto;
import com.okit.avis.dto.PageResponse;
import com.okit.avis.dto.ProductRatingDto;
import com.okit.avis.entities.Avis;
import com.okit.avis.exceptions.BusinessValidationException;
import com.okit.avis.exceptions.ResourceNotFoundException;
import com.okit.avis.mappers.AvisMapper;
import com.okit.avis.repositories.AvisRepository;
import com.okit.avis.repositories.RatingStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvisServiceTest {

    @Mock
    private AvisRepository avisRepository;

    @Spy
    private AvisMapper avisMapper = new AvisMapper();

    @InjectMocks
    private AvisServiceImpl avisService;

    private Avis sampleAvis;

    @BeforeEach
    void setUp() {
        sampleAvis = new Avis(1L, 100L, 200L, 4, "Très bon maillot", LocalDate.now());
    }

    @Test
    @DisplayName("Création nominale d'un avis avec persistance et datePublication initialisée à aujourd'hui")
    void createReview_ShouldPersistReviewAndSetPublicationDate() {
        AvisCreateRequestDto request = new AvisCreateRequestDto(
                100L,
                200L,
                5,
                "Super qualité !"
        );

        when(avisRepository.save(any(Avis.class))).thenAnswer(invocation -> {
            Avis toSave = invocation.getArgument(0);
            toSave.setIdAvis(42L);
            return toSave;
        });

        AvisResponseDto result = avisService.createReview(request);

        assertThat(result).isNotNull();
        assertThat(result.idAvis()).isEqualTo(42L);
        assertThat(result.idProduit()).isEqualTo(100L);
        assertThat(result.idUtilisateur()).isEqualTo(200L);
        assertThat(result.note()).isEqualTo(5);
        assertThat(result.commentaire()).isEqualTo("Super qualité !");
        assertThat(result.datePublication()).isEqualTo(LocalDate.now());

        ArgumentCaptor<Avis> captor = ArgumentCaptor.forClass(Avis.class);
        verify(avisRepository).save(captor.capture());
        Avis captured = captor.getValue();
        assertThat(captured.getDatePublication()).isEqualTo(LocalDate.now());
        assertThat(captured.getIdProduit()).isEqualTo(100L);
        assertThat(captured.getIdUtilisateur()).isEqualTo(200L);
        assertThat(captured.getNote()).isEqualTo(5);
        assertThat(captured.getCommentaire()).isEqualTo("Super qualité !");
    }

    @Test
    @DisplayName("Création d'un avis avec requête nulle -> BusinessValidationException")
    void createReview_WhenRequestIsNull_ShouldThrowBusinessValidationException() {
        assertThatThrownBy(() -> avisService.createReview(null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Les données de l'avis sont obligatoires");

        verify(avisRepository, never()).save(any());
    }

    @Test
    @DisplayName("Calcul de la note moyenne avec arrondi à 1 décimale (4.333 -> 4.3)")
    void getProductRating_ShouldRoundAverageToSingleDecimal_CaseOne() {
        Long productId = 10L;
        RatingStats stats = new RatingStats() {
            @Override
            public Double getMoyenne() {
                return 4.3333333333;
            }

            @Override
            public Long getTotal() {
                return 15L;
            }
        };

        when(avisRepository.findRatingStatsByProduitId(productId)).thenReturn(stats);

        ProductRatingDto rating = avisService.getProductRating(productId);

        assertThat(rating).isNotNull();
        assertThat(rating.idProduit()).isEqualTo(productId);
        assertThat(rating.noteMoyenne()).isEqualTo(4.3);
        assertThat(rating.totalAvis()).isEqualTo(15L);
    }

    @Test
    @DisplayName("Calcul de la note moyenne avec arrondi supérieur (3.45 -> 3.5)")
    void getProductRating_ShouldRoundAverageToSingleDecimal_CaseTwo() {
        Long productId = 11L;
        RatingStats stats = new RatingStats() {
            @Override
            public Double getMoyenne() {
                return 3.45;
            }

            @Override
            public Long getTotal() {
                return 20L;
            }
        };

        when(avisRepository.findRatingStatsByProduitId(productId)).thenReturn(stats);

        ProductRatingDto rating = avisService.getProductRating(productId);

        assertThat(rating).isNotNull();
        assertThat(rating.idProduit()).isEqualTo(productId);
        assertThat(rating.noteMoyenne()).isEqualTo(3.5);
        assertThat(rating.totalAvis()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Produit sans avis : total == 0 -> note_moyenne = 0.0 et total_avis = 0")
    void getProductRating_WhenTotalIsZero_ShouldReturnZeroStats() {
        Long productId = 12L;
        RatingStats stats = new RatingStats() {
            @Override
            public Double getMoyenne() {
                return null;
            }

            @Override
            public Long getTotal() {
                return 0L;
            }
        };

        when(avisRepository.findRatingStatsByProduitId(productId)).thenReturn(stats);

        ProductRatingDto rating = avisService.getProductRating(productId);

        assertThat(rating).isNotNull();
        assertThat(rating.idProduit()).isEqualTo(productId);
        assertThat(rating.noteMoyenne()).isEqualTo(0.0);
        assertThat(rating.totalAvis()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Produit sans avis : stats est null -> note_moyenne = 0.0 et total_avis = 0")
    void getProductRating_WhenStatsIsNull_ShouldReturnZeroStats() {
        Long productId = 13L;
        when(avisRepository.findRatingStatsByProduitId(productId)).thenReturn(null);

        ProductRatingDto rating = avisService.getProductRating(productId);

        assertThat(rating).isNotNull();
        assertThat(rating.idProduit()).isEqualTo(productId);
        assertThat(rating.noteMoyenne()).isEqualTo(0.0);
        assertThat(rating.totalAvis()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Produit sans avis : moyenne est nulle mais total > 0 -> note_moyenne = 0.0 et total_avis = 0")
    void getProductRating_WhenMoyenneIsNull_ShouldReturnZeroStats() {
        Long productId = 14L;
        RatingStats stats = new RatingStats() {
            @Override
            public Double getMoyenne() {
                return null;
            }

            @Override
            public Long getTotal() {
                return 5L;
            }
        };

        when(avisRepository.findRatingStatsByProduitId(productId)).thenReturn(stats);

        ProductRatingDto rating = avisService.getProductRating(productId);

        assertThat(rating).isNotNull();
        assertThat(rating.idProduit()).isEqualTo(productId);
        assertThat(rating.noteMoyenne()).isEqualTo(0.0);
        assertThat(rating.totalAvis()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Récupération des avis paginés par produit")
    void getReviewsByProduct_ShouldReturnPagedResponse() {
        Long productId = 100L;
        Pageable pageable = PageRequest.of(0, 5);
        Page<Avis> avisPage = new PageImpl<>(List.of(sampleAvis), pageable, 1);

        when(avisRepository.findByIdProduit(productId, pageable)).thenReturn(avisPage);

        PageResponse<AvisResponseDto> response = avisService.getReviewsByProduct(productId, pageable);

        assertThat(response).isNotNull();
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(5);
        assertThat(response.totalElements()).isEqualTo(1L);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().idAvis()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Récupération des avis par utilisateur")
    void getReviewsByUser_ShouldReturnList() {
        Long userId = 200L;
        when(avisRepository.findByIdUtilisateur(userId)).thenReturn(List.of(sampleAvis));

        List<AvisResponseDto> result = avisService.getReviewsByUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().idUtilisateur()).isEqualTo(userId);
        assertThat(result.getFirst().idAvis()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Modification d'un avis existant : note et commentaire mis à jour")
    void updateReview_WhenExists_ShouldUpdateNoteAndCommentaire() {
        Long avisId = 1L;
        AvisUpdateRequestDto request = new AvisUpdateRequestDto(5, "Commentaire modifié");

        when(avisRepository.findById(avisId)).thenReturn(Optional.of(sampleAvis));
        when(avisRepository.save(sampleAvis)).thenReturn(sampleAvis);

        AvisResponseDto result = avisService.updateReview(avisId, request);

        assertThat(result).isNotNull();
        assertThat(result.note()).isEqualTo(5);
        assertThat(result.commentaire()).isEqualTo("Commentaire modifié");
        assertThat(sampleAvis.getNote()).isEqualTo(5);
        assertThat(sampleAvis.getCommentaire()).isEqualTo("Commentaire modifié");
        verify(avisRepository).save(sampleAvis);
    }

    @Test
    @DisplayName("Modification d'un avis existant : note uniquement mise à jour")
    void updateReview_WhenOnlyNoteProvided_ShouldUpdateOnlyNote() {
        Long avisId = 1L;
        AvisUpdateRequestDto request = new AvisUpdateRequestDto(3, null);

        when(avisRepository.findById(avisId)).thenReturn(Optional.of(sampleAvis));
        when(avisRepository.save(sampleAvis)).thenReturn(sampleAvis);

        AvisResponseDto result = avisService.updateReview(avisId, request);

        assertThat(result.note()).isEqualTo(3);
        assertThat(result.commentaire()).isEqualTo("Très bon maillot");
        assertThat(sampleAvis.getNote()).isEqualTo(3);
        assertThat(sampleAvis.getCommentaire()).isEqualTo("Très bon maillot");
    }

    @Test
    @DisplayName("Modification d'un avis existant : commentaire uniquement mis à jour")
    void updateReview_WhenOnlyCommentaireProvided_ShouldUpdateOnlyCommentaire() {
        Long avisId = 1L;
        AvisUpdateRequestDto request = new AvisUpdateRequestDto(null, "Nouveau texte");

        when(avisRepository.findById(avisId)).thenReturn(Optional.of(sampleAvis));
        when(avisRepository.save(sampleAvis)).thenReturn(sampleAvis);

        AvisResponseDto result = avisService.updateReview(avisId, request);

        assertThat(result.note()).isEqualTo(4);
        assertThat(result.commentaire()).isEqualTo("Nouveau texte");
        assertThat(sampleAvis.getNote()).isEqualTo(4);
        assertThat(sampleAvis.getCommentaire()).isEqualTo("Nouveau texte");
    }

    @Test
    @DisplayName("Modification d'un avis inexistant -> ResourceNotFoundException")
    void updateReview_WhenNotFound_ShouldThrowResourceNotFoundException() {
        Long nonExistentId = 999L;
        AvisUpdateRequestDto request = new AvisUpdateRequestDto(5, "Texte");

        when(avisRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avisService.updateReview(nonExistentId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Avis non trouvé avec l'identifiant : 999");

        verify(avisRepository, never()).save(any());
    }

    @Test
    @DisplayName("Suppression d'un avis existant")
    void deleteReview_WhenExists_ShouldDelete() {
        Long avisId = 1L;
        when(avisRepository.findById(avisId)).thenReturn(Optional.of(sampleAvis));

        avisService.deleteReview(avisId);

        verify(avisRepository).delete(sampleAvis);
    }

    @Test
    @DisplayName("Suppression d'un avis inexistant -> ResourceNotFoundException")
    void deleteReview_WhenNotFound_ShouldThrowResourceNotFoundException() {
        Long nonExistentId = 999L;
        when(avisRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avisService.deleteReview(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Avis non trouvé avec l'identifiant : 999");

        verify(avisRepository, never()).delete(any());
    }
}
