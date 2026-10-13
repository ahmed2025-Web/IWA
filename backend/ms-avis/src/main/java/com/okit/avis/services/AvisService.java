package com.okit.avis.services;

import com.okit.avis.dto.AvisCreateRequestDto;
import com.okit.avis.dto.AvisResponseDto;
import com.okit.avis.dto.AvisUpdateRequestDto;
import com.okit.avis.dto.PageResponse;
import com.okit.avis.dto.ProductRatingDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AvisService {

    AvisResponseDto createReview(AvisCreateRequestDto request);

    PageResponse<AvisResponseDto> getReviewsByProduct(Long idProduit, Pageable pageable);

    List<AvisResponseDto> getReviewsByUser(Long idUtilisateur);

    ProductRatingDto getProductRating(Long idProduit);

    AvisResponseDto updateReview(Long idAvis, AvisUpdateRequestDto request);

    void deleteReview(Long idAvis);
}
