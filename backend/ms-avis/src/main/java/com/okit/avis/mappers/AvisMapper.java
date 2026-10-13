package com.okit.avis.mappers;

import com.okit.avis.dto.AvisCreateRequestDto;
import com.okit.avis.dto.AvisResponseDto;
import com.okit.avis.entities.Avis;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AvisMapper {

    public AvisResponseDto toDto(Avis avis) {
        if (avis == null) {
            return null;
        }
        return new AvisResponseDto(
                avis.getIdAvis(),
                avis.getIdProduit(),
                avis.getIdUtilisateur(),
                avis.getNote(),
                avis.getCommentaire(),
                avis.getDatePublication()
        );
    }

    public Avis toEntity(AvisCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Avis avis = new Avis();
        avis.setIdProduit(dto.idProduit());
        avis.setIdUtilisateur(dto.idUtilisateur());
        avis.setNote(dto.note());
        avis.setCommentaire(dto.commentaire());
        avis.setDatePublication(LocalDate.now());
        return avis;
    }
}
