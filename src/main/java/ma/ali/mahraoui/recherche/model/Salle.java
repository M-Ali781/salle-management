package ma.ali.mahraoui.recherche.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entité représentant une salle de réunion ou de conférence.
 * Chaque salle possède un nom unique, une capacité d'accueil et un ensemble de réservations associées.
 * 
 * @author Ali Mahraoui
 */
@Entity
@Table(name = "salles")
public class Salle implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 100)
    private String nom;

    @Column(name = "capacite", nullable = false)
    private int capacite;

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Reservation> reservations = new LinkedHashSet<>();

    /**
     * Constructeur par défaut requis par la spécification JPA.
     */
    public Salle() {
        super();
    }

    /**
     * Constructeur avec paramètres principaux.
     *
     * @param nom      le libellé unique de la salle
     * @param capacite la capacité maximale d'accueil
     */
    public Salle(String nom, int capacite) {
        this.nom = (nom != null) ? nom.trim() : null;
        this.capacite = capacite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = (nom != null) ? nom.trim() : null;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    /**
     * Retourne une vue non-modifiable des réservations de cette salle.
     */
    public Set<Reservation> getReservations() {
        return Collections.unmodifiableSet(reservations);
    }

    /**
     * Associe une réservation à la salle en maintenant la cohérence bidirectionnelle.
     *
     * @param reservation la réservation à ajouter
     */
    public void ajouterReservation(Reservation reservation) {
        if (reservation != null) {
            reservation.setSalle(this);
        }
    }

    /**
     * Méthode interne de synchronisation bidirectionnelle (appelée par Reservation.setSalle).
     */
    void associer(Reservation reservation) {
        if (reservation != null) {
            this.reservations.add(reservation);
        }
    }

    /**
     * Méthode interne de dissociation bidirectionnelle (appelée par Reservation.setSalle).
     */
    void dissocier(Reservation reservation) {
        if (reservation != null) {
            this.reservations.remove(reservation);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Salle salle = (Salle) o;
        if (id != null && salle.id != null) {
            return Objects.equals(id, salle.id);
        }
        return Objects.equals(nom, salle.nom);
    }

    @Override
    public int hashCode() {
        return (id != null) ? Objects.hash(id) : Objects.hash(nom);
    }

    @Override
    public String toString() {
        return "Salle{id=" + id + ", nom='" + nom + "', capacite=" + capacite + "}";
    }
}
