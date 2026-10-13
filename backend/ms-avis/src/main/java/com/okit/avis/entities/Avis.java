package com.okit.avis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "avis")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avis")
    private Long idAvis;

    @NotNull(message = "L'identifiant du produit est obligatoire")
    @Column(name = "id_produit", nullable = false)
    private Long idProduit;

    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    @Column(name = "id_utilisateur", nullable = false)
    private Long idUtilisateur;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note doit être au minimum de 1")
    @Max(value = 5, message = "La note doit être au maximum de 5")
    @Column(name = "note", nullable = false)
    private Integer note;

    @NotBlank(message = "Le commentaire est obligatoire")
    @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
    @Column(name = "commentaire", length = 1000, nullable = false)
    private String commentaire;

    @NotNull(message = "La date de publication est obligatoire")
    @Column(name = "date_publication", nullable = false)
    private LocalDate datePublication = LocalDate.now();

    public Avis() {
        this.datePublication = LocalDate.now();
    }

    public Avis(Long idProduit, Long idUtilisateur, Integer note, String commentaire) {
        this.idProduit = idProduit;
        this.idUtilisateur = idUtilisateur;
        this.note = note;
        this.commentaire = commentaire;
        this.datePublication = LocalDate.now();
    }

    public Avis(Long idProduit, Long idUtilisateur, Integer note, String commentaire, LocalDate datePublication) {
        this.idProduit = idProduit;
        this.idUtilisateur = idUtilisateur;
        this.note = note;
        this.commentaire = commentaire;
        this.datePublication = datePublication != null ? datePublication : LocalDate.now();
    }

    public Avis(Long idAvis, Long idProduit, Long idUtilisateur, Integer note, String commentaire, LocalDate datePublication) {
        this.idAvis = idAvis;
        this.idProduit = idProduit;
        this.idUtilisateur = idUtilisateur;
        this.note = note;
        this.commentaire = commentaire;
        this.datePublication = datePublication != null ? datePublication : LocalDate.now();
    }

    public Long getIdAvis() {
        return idAvis;
    }

    public void setIdAvis(Long idAvis) {
        this.idAvis = idAvis;
    }

    public Long getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(Long idProduit) {
        this.idProduit = idProduit;
    }

    public Long getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Long idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDate getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDate datePublication) {
        this.datePublication = datePublication;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Avis avis = (Avis) o;
        return Objects.equals(idAvis, avis.idAvis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAvis);
    }

    @Override
    public String toString() {
        return "Avis{" +
                "idAvis=" + idAvis +
                ", idProduit=" + idProduit +
                ", idUtilisateur=" + idUtilisateur +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", datePublication=" + datePublication +
                '}';
    }
}
