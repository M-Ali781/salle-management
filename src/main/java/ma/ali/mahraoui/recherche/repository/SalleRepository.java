package ma.ali.mahraoui.recherche.repository;

import java.time.LocalDateTime;
import java.util.List;
import ma.ali.mahraoui.recherche.model.Salle;

/**
 * Interface d'accès aux données pour l'entité {@link Salle}.
 * Fournit des opérations de recherche multicritères avancées et de contrôle de disponibilité temporelle.
 *
 * @author Ali Mahraoui
 */
public interface SalleRepository {

    /**
     * Recherche les salles libres sur un intervalle de temps donné.
     *
     * @param debut date et heure de début
     * @param fin   date et heure de fin
     * @return liste des salles ne disposant d'aucun chevauchement de réservation
     */
    List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin);

    /**
     * Recherche multi-critères avec pagination sur les salles.
     *
     * @param nom         fragment du nom recherché (insensible à la casse, optionnel)
     * @param capaciteMin capacité minimale requise (optionnelle)
     * @param debut       début du créneau souhaité (optionnel)
     * @param fin         fin du créneau souhaité (optionnel)
     * @param page        numéro de page (1-indexé)
     * @param size        nombre maximal de résultats par page
     * @return liste ordonnée des salles correspondant aux critères
     */
    List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size);

    /**
     * Dénombre le nombre total de salles répondant aux critères de recherche.
     *
     * @param nom         fragment du nom recherché (optionnel)
     * @param capaciteMin capacité minimale (optionnelle)
     * @param debut       début du créneau (optionnel)
     * @param fin         fin du créneau (optionnel)
     * @return nombre total d'enregistrements correspondants
     */
    long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin);
}
