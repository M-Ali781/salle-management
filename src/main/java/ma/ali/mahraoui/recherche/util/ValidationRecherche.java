package ma.ali.mahraoui.recherche.util;

import java.time.LocalDateTime;

/**
 * Classe utilitaire pour la validation stricte des paramètres de recherche et des règles métier.
 * 
 * @author Ali Mahraoui
 */
public final class ValidationRecherche {

    private ValidationRecherche() {
        // Empêche l'instanciation de cette classe utilitaire
    }

    /**
     * Valide la cohérence temporelle d'un créneau horaire.
     *
     * @param debut       date/heure de début
     * @param fin         date/heure de fin
     * @param obligatoire indique si le créneau est requis
     * @throws IllegalArgumentException si les dates sont nulles ou incohérentes
     */
    public static void creneau(LocalDateTime debut, LocalDateTime fin, boolean obligatoire) {
        if (!obligatoire && debut == null && fin == null) {
            return;
        }

        if (debut == null || fin == null) {
            throw new IllegalArgumentException("Le créneau horaire doit spécifier à la fois une date de début et de fin.");
        }

        if (!debut.isBefore(fin)) {
            throw new IllegalArgumentException("Le créneau doit contenir un début strictement antérieur à la fin.");
        }
    }

    /**
     * Valide l'ensemble des filtres de recherche multi-critères.
     *
     * @param capaciteMin capacité minimale souhaitée (optionnelle)
     * @param debut       début du créneau (optionnel)
     * @param fin         fin du créneau (optionnel)
     * @throws IllegalArgumentException si un critère est invalide
     */
    public static void filtres(Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        if (capaciteMin != null && capaciteMin < 0) {
            throw new IllegalArgumentException("La capacité minimale ne peut pas être négative.");
        }
        creneau(debut, fin, false);
    }

    /**
     * Calcule et vérifie l'offset pour la pagination JPA.
     *
     * @param page numéro de page (1-indexé)
     * @param size taille de la page (nombre d'éléments)
     * @return l'offset calculé
     * @throws IllegalArgumentException si les bornes sont invalides ou dépassent Integer.MAX_VALUE
     */
    public static int offset(int page, int size) {
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("Le numéro de page et sa taille doivent être positifs.");
        }

        long index = ((long) page - 1) * size;
        if (index > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Le décalage dépasse la limite de pagination JPA.");
        }

        return (int) index;
    }
}
