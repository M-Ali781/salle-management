package ma.ali.mahraoui.recherche.service;

import java.time.LocalDateTime;
import java.util.List;
import ma.ali.mahraoui.recherche.model.Salle;
import ma.ali.mahraoui.recherche.util.PaginationResult;

/**
 * Service métier dédié à la consultation et à la recherche avancée de salles.
 *
 * @author Ali Mahraoui
 */
public interface SalleService {

    /**
     * Obtient la liste des salles libres sur l'ensemble de la plage horaire spécifiée.
     *
     * @param debut date et heure de début
     * @param fin   date et heure de fin
     * @return liste des salles disponibles
     */
    List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin);

    /**
     * Recherche paginée de salles selon des filtres combinables.
     *
     * @param nom         fragment du nom recherché
     * @param capaciteMin seuil minimum de capacité
     * @param debut       début de disponibilité exigée
     * @param fin         fin de disponibilité exigée
     * @param page        numéro de page (1-indexé)
     * @param size        taille de la page
     * @return liste d'éléments de la page
     */
    List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size);

    /**
     * Dénombre le total de salles répondant aux critères indiqués.
     *
     * @param nom         fragment du nom recherché
     * @param capaciteMin seuil minimum de capacité
     * @param debut       début de disponibilité
     * @param fin         fin de disponibilité
     * @return nombre total d'enregistrements correspondants
     */
    long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin);

    /**
     * Effectue une recherche complète restituant un conteneur paginé avec métadonnées.
     *
     * @param nom         fragment de nom
     * @param capaciteMin capacité minimale
     * @param debut       début de plage horaire
     * @param fin         fin de plage horaire
     * @param page        numéro de page
     * @param size        taille de page
     * @return instance de {@link PaginationResult} contenant les données et indicateurs
     */
    PaginationResult<Salle> searchPage(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size);
}
