package ma.ali.mahraoui.recherche.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import ma.ali.mahraoui.recherche.util.ValidationRecherche;

/**
 * Entité modélisant une réservation de salle sur un créneau temporel déterminé.
 * Intègre des index d'optimisation et la validation métier automatique des dates.
 * 
 * @author Ali Mahraoui
 */
@Entity
@Table(name = "reservations", indexes = {
    @Index(name = "idx_reservation_creneau", columnList = "salle_id,debut,fin")
})
public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @Column(name = "debut", nullable = false)
    private LocalDateTime debut;

    @Column(name = "fin", nullable = false)
    private LocalDateTime fin;

    /**
     * Constructeur par défaut requis par JPA.
     */
    public Reservation() {
        super();
    }

    /**
     * Constructeur complet liant une salle à une période de réservation.
     *
     * @param salle la salle à réserver
     * @param debut date et heure de début
     * @param fin   date et heure de fin
     */
    public Reservation(Salle salle, LocalDateTime debut, LocalDateTime fin) {
        modifierPeriode(debut, fin);
        setSalle(salle);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Salle getSalle() {
        return salle;
    }

    /**
     * Définit la salle associée en gérant la synchronisation de la relation bidirectionnelle.
     */
    public void setSalle(Salle nouvelleSalle) {
        if (this.salle == nouvelleSalle) {
            return;
        }
        if (this.salle != null) {
            this.salle.dissocier(this);
        }
        this.salle = nouvelleSalle;
        if (this.salle != null) {
            this.salle.associer(this);
        }
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    /**
     * Met à jour l'intervalle horaire de la réservation avec contrôle de validité.
     */
    public void modifierPeriode(LocalDateTime debut, LocalDateTime fin) {
        ValidationRecherche.creneau(debut, fin, true);
        this.debut = debut;
        this.fin = fin;
    }

    /**
     * Hook de cycle de vie JPA exécuté avant la persistance ou la mise à jour.
     */
    @PrePersist
    @PreUpdate
    private void valider() {
        ValidationRecherche.creneau(debut, fin, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(salle, that.salle) &&
               Objects.equals(debut, that.debut) &&
               Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        return (id != null) ? Objects.hash(id) : Objects.hash(salle, debut, fin);
    }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", debut=" + debut + ", fin=" + fin + "}";
    }
}
