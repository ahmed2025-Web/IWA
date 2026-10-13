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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AvisServiceImpl implements AvisService {

    private final AvisRepository avisRepository;
    private final AvisMapper avisMapper;

    @Autowired
    public AvisServiceImpl(AvisRepository avisRepository, AvisMapper avisMapper) {
        this.avisRepository = avisRepository;
        this.avisMapper = avisMapper;
    }

    @Override
    public AvisResponseDto createReview(AvisCreateRequestDto request) {
        if (request == null) {
            throw new BusinessValidationException("Les données de l'avis sont obligatoires");
        }

        Avis avis = avisMapper.toEntity(request);
        avis.setDatePublication(LocalDate.now());

        Avis savedAvis = avisRepository.save(avis);
        return avisMapper.toDto(savedAvis);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AvisResponseDto> getReviewsByProduct(Long idProduit, Pageable pageable) {
        Page<Avis> page = avisRepository.findByIdProduit(idProduit, pageable);
        List<AvisResponseDto> dtos = page.getContent().stream()
                .map(avisMapper::toDto)
                .toList();

        return new PageResponse<>(
                dtos,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisResponseDto> getReviewsByUser(Long idUtilisateur) {
        return avisRepository.findByIdUtilisateur(idUtilisateur).stream()
                .map(avisMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRatingDto getProductRating(Long idProduit) {
        RatingStats stats = avisRepository.findRatingStatsByProduitId(idProduit);

        if (stats == null || stats.getTotal() == null || stats.getTotal() == 0L || stats.getMoyenne() == null) {
            return new ProductRatingDto(idProduit, 0.0, 0L);
        }

        double noteMoyenneArrondie = BigDecimal.valueOf(stats.getMoyenne())
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        return new ProductRatingDto(idProduit, noteMoyenneArrondie, stats.getTotal());
    }

    @Override
    public AvisResponseDto updateReview(Long idAvis, AvisUpdateRequestDto request) {
        Avis avis = avisRepository.findById(idAvis)
                .orElseThrow(() -> new ResourceNotFoundException("Avis non trouvé avec l'identifiant : " + idAvis));

        if (request != null) {
            if (request.note() != null) {
                avis.setNote(request.note());
            }
            if (request.commentaire() != null) {
                avis.setCommentaire(request.commentaire());
            }
        }

        Avis updatedAvis = avisRepository.save(avis);
        return avisMapper.toDto(updatedAvis);
    }

    @Override
    public void deleteReview(Long idAvis) {
        Avis avis = avisRepository.findById(idAvis)
                .orElseThrow(() -> new ResourceNotFoundException("Avis non trouvé avec l'identifiant : " + idAvis));

        avisRepository.delete(avis);
    }
}
