package com.okit.avis.repositories;

import com.okit.avis.entities.Avis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class AvisRepositoryTest {

    @Autowired
    private AvisRepository avisRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Cas 1 : Création et persistance d'un avis valide")
    void shouldCreateAndPersistValidAvis() {
        Avis avis = new Avis(101L, 201L, 5, "Excellent produit, très confortable !");
        Avis saved = avisRepository.save(avis);
        entityManager.flush();
        entityManager.clear();

        assertThat(saved.getIdAvis()).isNotNull();

        Optional<Avis> found = avisRepository.findById(saved.getIdAvis());
        assertThat(found).isPresent();
        assertThat(found.get().getIdProduit()).isEqualTo(101L);
        assertThat(found.get().getIdUtilisateur()).isEqualTo(201L);
        assertThat(found.get().getNote()).isEqualTo(5);
        assertThat(found.get().getCommentaire()).isEqualTo("Excellent produit, très confortable !");
        assertThat(found.get().getDatePublication()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Cas 2 : Pagination des avis pour un produit donné")
    void shouldFindAvisByIdProduitWithPagination() {
        Long idProduit = 102L;
        avisRepository.save(new Avis(idProduit, 1L, 4, "Très bon"));
        avisRepository.save(new Avis(idProduit, 2L, 5, "Parfait"));
        avisRepository.save(new Avis(idProduit, 3L, 3, "Moyen"));
        avisRepository.save(new Avis(999L, 4L, 5, "Autre produit"));
        entityManager.flush();
        entityManager.clear();

        Page<Avis> page0 = avisRepository.findByIdProduit(idProduit, PageRequest.of(0, 2, Sort.by("idAvis").ascending()));
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page0.getContent()).hasSize(2);

        Page<Avis> page1 = avisRepository.findByIdProduit(idProduit, PageRequest.of(1, 2, Sort.by("idAvis").ascending()));
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent().get(0).getNote()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cas 3 : Recherche des avis par utilisateur")
    void shouldFindAvisByIdUtilisateur() {
        Long idUser = 301L;
        avisRepository.save(new Avis(10L, idUser, 4, "Avis 1 utilisateur"));
        avisRepository.save(new Avis(20L, idUser, 5, "Avis 2 utilisateur"));
        avisRepository.save(new Avis(30L, 999L, 2, "Avis d'un autre utilisateur"));
        entityManager.flush();
        entityManager.clear();

        List<Avis> avisList = avisRepository.findByIdUtilisateur(idUser);
        assertThat(avisList).hasSize(2);
        assertThat(avisList).extracting(Avis::getIdProduit).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    @DisplayName("Cas 4 : Calcul d'agrégation sur un produit ayant plusieurs avis (notes 3, 4 et 5)")
    void shouldCalculateRatingStatsForProductWithMultipleAvis() {
        Long idProduit = 103L;
        avisRepository.save(new Avis(idProduit, 1L, 3, "Correct"));
        avisRepository.save(new Avis(idProduit, 2L, 4, "Bon"));
        avisRepository.save(new Avis(idProduit, 3L, 5, "Excellent"));
        avisRepository.save(new Avis(888L, 4L, 1, "Autre produit"));
        entityManager.flush();
        entityManager.clear();

        RatingStats stats = avisRepository.findRatingStatsByProduitId(idProduit);
        assertThat(stats).isNotNull();
        assertThat(stats.getTotal()).isEqualTo(3L);
        assertThat(stats.getMoyenne()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("Cas 5 : Calcul d'agrégation sur un produit sans aucun avis")
    void shouldCalculateRatingStatsForProductWithoutAvis() {
        Long idProduitSansAvis = 9999L;

        RatingStats stats = avisRepository.findRatingStatsByProduitId(idProduitSansAvis);
        assertThat(stats).isNotNull();
        assertThat(stats.getTotal()).isEqualTo(0L);
        assertThat(stats.getMoyenne()).isNull();
    }
}
