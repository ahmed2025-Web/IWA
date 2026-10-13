package com.okit.avis.controllers;

import com.okit.avis.dto.AvisCreateRequestDto;
import com.okit.avis.dto.AvisResponseDto;
import com.okit.avis.dto.AvisUpdateRequestDto;
import com.okit.avis.dto.PageResponse;
import com.okit.avis.dto.ProductRatingDto;
import com.okit.avis.services.AvisService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final AvisService avisService;

    public ReviewController(AvisService avisService) {
        this.avisService = avisService;
    }

    @PostMapping
    public ResponseEntity<AvisResponseDto> createReview(@Valid @RequestBody AvisCreateRequestDto request) {
        AvisResponseDto response = avisService.createReview(request);
        URI location = URI.create("/api/v1/reviews/" + response.idAvis());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/product/{id_produit}")
    public ResponseEntity<PageResponse<AvisResponseDto>> getReviewsByProduct(
            @PathVariable("id_produit") Long idProduit,
            @PageableDefault(size = 10, sort = "datePublication") Pageable pageable) {
        PageResponse<AvisResponseDto> response = avisService.getReviewsByProduct(idProduit, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{id_produit}/rating")
    public ResponseEntity<ProductRatingDto> getProductRating(@PathVariable("id_produit") Long idProduit) {
        ProductRatingDto response = avisService.getProductRating(idProduit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{id_utilisateur}")
    public ResponseEntity<List<AvisResponseDto>> getReviewsByUser(@PathVariable("id_utilisateur") Long idUtilisateur) {
        List<AvisResponseDto> response = avisService.getReviewsByUser(idUtilisateur);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id_avis}")
    public ResponseEntity<AvisResponseDto> updateReview(
            @PathVariable("id_avis") Long idAvis,
            @Valid @RequestBody AvisUpdateRequestDto request) {
        AvisResponseDto response = avisService.updateReview(idAvis, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id_avis}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id_avis") Long idAvis) {
        avisService.deleteReview(idAvis);
        return ResponseEntity.noContent().build();
    }
}
