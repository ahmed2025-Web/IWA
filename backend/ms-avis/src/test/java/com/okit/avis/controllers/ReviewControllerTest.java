package com.okit.avis.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okit.avis.dto.AvisCreateRequestDto;
import com.okit.avis.dto.AvisResponseDto;
import com.okit.avis.dto.AvisUpdateRequestDto;
import com.okit.avis.dto.PageResponse;
import com.okit.avis.dto.ProductRatingDto;
import com.okit.avis.exceptions.BusinessValidationException;
import com.okit.avis.exceptions.GlobalExceptionHandler;
import com.okit.avis.exceptions.ResourceNotFoundException;
import com.okit.avis.services.AvisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import(GlobalExceptionHandler.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AvisService avisService;

    @Nested
    @DisplayName("Flots nominaux (Happy Paths)")
    class NominalFlows {

        @Test
        @DisplayName("POST /api/v1/reviews valide -> 201 CREATED avec Location header et DTO")
        void createReview_ValidPayload_Returns201Created() throws Exception {
            AvisCreateRequestDto request = new AvisCreateRequestDto(
                    101L,
                    201L,
                    5,
                    "Excellent maillot, tissu respirant !"
            );

            AvisResponseDto response = new AvisResponseDto(
                    1L,
                    101L,
                    201L,
                    5,
                    "Excellent maillot, tissu respirant !",
                    LocalDate.of(2026, 10, 13)
            );

            when(avisService.createReview(any(AvisCreateRequestDto.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/v1/reviews/1"))
                    .andExpect(jsonPath("$.id_avis", is(1)))
                    .andExpect(jsonPath("$.id_produit", is(101)))
                    .andExpect(jsonPath("$.id_utilisateur", is(201)))
                    .andExpect(jsonPath("$.note", is(5)))
                    .andExpect(jsonPath("$.commentaire", is("Excellent maillot, tissu respirant !")))
                    .andExpect(jsonPath("$.date_publication", is("2026-10-13")));
        }

        @Test
        @DisplayName("GET /api/v1/reviews/product/{id_produit} -> 200 OK avec pagination PageResponse")
        void getReviewsByProduct_Returns200OkWithPageResponse() throws Exception {
            Long productId = 101L;
            AvisResponseDto review = new AvisResponseDto(
                    1L,
                    productId,
                    201L,
                    4,
                    "Très bon produit",
                    LocalDate.of(2026, 10, 13)
            );
            PageResponse<AvisResponseDto> pageResponse = new PageResponse<>(
                    List.of(review),
                    0,
                    10,
                    1L,
                    1
            );

            when(avisService.getReviewsByProduct(eq(productId), any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v1/reviews/product/{id_produit}", productId)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()", is(1)))
                    .andExpect(jsonPath("$.content[0].id_avis", is(1)))
                    .andExpect(jsonPath("$.content[0].id_produit", is(101)))
                    .andExpect(jsonPath("$.page", is(0)))
                    .andExpect(jsonPath("$.size", is(10)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)));
        }

        @Test
        @DisplayName("GET /api/v1/reviews/product/{id_produit}/rating -> 200 OK avec note_moyenne et total_avis")
        void getProductRating_WithReviews_Returns200OkWithRating() throws Exception {
            Long productId = 101L;
            ProductRatingDto ratingDto = new ProductRatingDto(productId, 4.3, 15L);

            when(avisService.getProductRating(productId)).thenReturn(ratingDto);

            mockMvc.perform(get("/api/v1/reviews/product/{id_produit}/rating", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id_produit", is(101)))
                    .andExpect(jsonPath("$.note_moyenne", is(4.3)))
                    .andExpect(jsonPath("$.total_avis", is(15)));
        }

        @Test
        @DisplayName("GET /api/v1/reviews/user/{id_utilisateur} -> 200 OK avec liste des avis")
        void getReviewsByUser_Returns200OkWithList() throws Exception {
            Long userId = 201L;
            AvisResponseDto review = new AvisResponseDto(
                    1L,
                    101L,
                    userId,
                    5,
                    "Super maillot",
                    LocalDate.of(2026, 10, 13)
            );

            when(avisService.getReviewsByUser(userId)).thenReturn(List.of(review));

            mockMvc.perform(get("/api/v1/reviews/user/{id_utilisateur}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(1)))
                    .andExpect(jsonPath("$[0].id_avis", is(1)))
                    .andExpect(jsonPath("$[0].id_utilisateur", is(201)));
        }

        @Test
        @DisplayName("PATCH /api/v1/reviews/{id_avis} valide -> 200 OK avec avis mis à jour")
        void updateReview_ValidPayload_Returns200Ok() throws Exception {
            Long reviewId = 1L;
            AvisUpdateRequestDto request = new AvisUpdateRequestDto(4, "Commentaire modifié");
            AvisResponseDto response = new AvisResponseDto(
                    reviewId,
                    101L,
                    201L,
                    4,
                    "Commentaire modifié",
                    LocalDate.of(2026, 10, 13)
            );

            when(avisService.updateReview(eq(reviewId), any(AvisUpdateRequestDto.class))).thenReturn(response);

            mockMvc.perform(patch("/api/v1/reviews/{id_avis}", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id_avis", is(1)))
                    .andExpect(jsonPath("$.note", is(4)))
                    .andExpect(jsonPath("$.commentaire", is("Commentaire modifié")));
        }

        @Test
        @DisplayName("DELETE /api/v1/reviews/{id_avis} existant -> 204 NO_CONTENT")
        void deleteReview_ExistingReview_Returns204NoContent() throws Exception {
            Long reviewId = 1L;
            doNothing().when(avisService).deleteReview(reviewId);

            mockMvc.perform(delete("/api/v1/reviews/{id_avis}", reviewId))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Flots alternatifs (Edge Cases)")
    class AlternativeFlows {

        @Test
        @DisplayName("GET rating sur produit sans avis -> 200 OK avec note_moyenne: 0.0 et total_avis: 0")
        void getProductRating_WhenNoReviews_Returns200OkWithZero() throws Exception {
            Long productId = 999L;
            ProductRatingDto ratingDto = new ProductRatingDto(productId, 0.0, 0L);

            when(avisService.getProductRating(productId)).thenReturn(ratingDto);

            mockMvc.perform(get("/api/v1/reviews/product/{id_produit}/rating", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id_produit", is(999)))
                    .andExpect(jsonPath("$.note_moyenne", is(0.0)))
                    .andExpect(jsonPath("$.total_avis", is(0)));
        }

        @Test
        @DisplayName("GET avis utilisateur sans avis -> 200 OK avec liste vide")
        void getReviewsByUser_WhenNoReviews_Returns200OkWithEmptyList() throws Exception {
            Long userId = 999L;
            when(avisService.getReviewsByUser(userId)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/reviews/user/{id_utilisateur}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()", is(0)));
        }
    }

    @Nested
    @DisplayName("Flots exceptionnels (Error & Validation Cases)")
    class ExceptionalFlows {

        @Test
        @DisplayName("POST avec note < 1 (ex: 0) -> 400 BAD_REQUEST")
        void createReview_WithNoteTooLow_Returns400BadRequest() throws Exception {
            AvisCreateRequestDto request = new AvisCreateRequestDto(
                    101L,
                    201L,
                    0,
                    "Très déçu"
            );

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.fieldErrors.note").exists());
        }

        @Test
        @DisplayName("POST avec note > 5 (ex: 6) -> 400 BAD_REQUEST")
        void createReview_WithNoteTooHigh_Returns400BadRequest() throws Exception {
            AvisCreateRequestDto request = new AvisCreateRequestDto(
                    101L,
                    201L,
                    6,
                    "Trop parfait"
            );

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.fieldErrors.note").exists());
        }

        @Test
        @DisplayName("POST avec commentaire vide/blank -> 400 BAD_REQUEST")
        void createReview_WithBlankCommentaire_Returns400BadRequest() throws Exception {
            AvisCreateRequestDto request = new AvisCreateRequestDto(
                    101L,
                    201L,
                    4,
                    "   "
            );

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.fieldErrors.commentaire").exists());
        }

        @Test
        @DisplayName("PATCH sur id_avis inexistant -> 404 NOT_FOUND")
        void updateReview_NonExistentReview_Returns404NotFound() throws Exception {
            Long nonExistentId = 999L;
            AvisUpdateRequestDto request = new AvisUpdateRequestDto(4, "Nouveau texte");

            when(avisService.updateReview(eq(nonExistentId), any(AvisUpdateRequestDto.class)))
                    .thenThrow(new ResourceNotFoundException("Avis non trouvé avec l'identifiant : " + nonExistentId));

            mockMvc.perform(patch("/api/v1/reviews/{id_avis}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("999")))
                    .andExpect(jsonPath("$.path", is("/api/v1/reviews/999")));
        }

        @Test
        @DisplayName("DELETE sur id_avis inexistant -> 404 NOT_FOUND")
        void deleteReview_NonExistentReview_Returns404NotFound() throws Exception {
            Long nonExistentId = 999L;
            doThrow(new ResourceNotFoundException("Avis non trouvé avec l'identifiant : " + nonExistentId))
                    .when(avisService).deleteReview(nonExistentId);

            mockMvc.perform(delete("/api/v1/reviews/{id_avis}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("999")))
                    .andExpect(jsonPath("$.path", is("/api/v1/reviews/999")));
        }

        @Test
        @DisplayName("Erreur de validation métier BusinessValidationException -> 400 BAD_REQUEST")
        void businessValidationException_Returns400BadRequest() throws Exception {
            AvisCreateRequestDto request = new AvisCreateRequestDto(
                    101L,
                    201L,
                    5,
                    "Superbe qualité"
            );

            when(avisService.createReview(any(AvisCreateRequestDto.class)))
                    .thenThrow(new BusinessValidationException("L'utilisateur a déjà posté un avis sur ce produit"));

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", containsString("L'utilisateur a déjà posté un avis")));
        }

        @Test
        @DisplayName("Erreur inattendue Exception -> 500 INTERNAL_SERVER_ERROR avec message sécurisé")
        void unexpectedException_Returns500InternalServerError() throws Exception {
            when(avisService.getProductRating(101L))
                    .thenThrow(new RuntimeException("Crash interne de base de données"));

            mockMvc.perform(get("/api/v1/reviews/product/101/rating"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status", is(500)))
                    .andExpect(jsonPath("$.error", is("Internal Server Error")))
                    .andExpect(jsonPath("$.message", is("Une erreur interne est survenue. Veuillez réessayer plus tard.")));
        }
    }
}
