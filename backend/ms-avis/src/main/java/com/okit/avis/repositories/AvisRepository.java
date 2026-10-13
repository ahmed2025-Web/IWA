package com.okit.avis.repositories;

import com.okit.avis.entities.Avis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {

    Page<Avis> findByIdProduit(Long idProduit, Pageable pageable);

    List<Avis> findByIdUtilisateur(Long idUtilisateur);

    @Query("SELECT AVG(a.note) as moyenne, COUNT(a) as total FROM Avis a WHERE a.idProduit = :idProduit")
    RatingStats findRatingStatsByProduitId(@Param("idProduit") Long idProduit);
}
