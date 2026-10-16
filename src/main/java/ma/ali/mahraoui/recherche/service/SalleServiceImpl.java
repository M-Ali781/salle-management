package ma.ali.mahraoui.recherche.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import ma.ali.mahraoui.recherche.model.Salle;
import ma.ali.mahraoui.recherche.repository.SalleRepository;
import ma.ali.mahraoui.recherche.util.PaginationResult;
import ma.ali.mahraoui.recherche.util.ValidationRecherche;

/**
 * Implémentation du service métier {@link SalleService}.
 * Coordonne la validation des règles de gestion et délègue l'accès aux données au {@link SalleRepository}.
 *
 * @author Ali Mahraoui
 */
public class SalleServiceImpl implements SalleService {

    private final SalleRepository salleRepository;

    /**
     * Constructeur avec injection explicite de dépendance.
     * Compatible avec les environnements Java SE standards et conteneurs IoC/CDI.
     *
     * @param salleRepository composant d'accès aux données des salles
     */
    public SalleServiceImpl(SalleRepository salleRepository) {
        this.salleRepository = Objects.requireNonNull(salleRepository, "Le repository de salles ne peut pas être null.");
    }

    @Override
    public List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        ValidationRecherche.creneau(debut, fin, true);
        return salleRepository.findAvailableRooms(debut, fin);
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size) {
        ValidationRecherche.filtres(capaciteMin, debut, fin);
        ValidationRecherche.offset(page, size);
        return salleRepository.searchRooms(nom, capaciteMin, debut, fin, page, size);
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        ValidationRecherche.filtres(capaciteMin, debut, fin);
        return salleRepository.countRooms(nom, capaciteMin, debut, fin);
    }

    @Override
    public PaginationResult<Salle> searchPage(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size) {
        List<Salle> elements = searchRooms(nom, capaciteMin, debut, fin, page, size);
        long totalResultats = countRooms(nom, capaciteMin, debut, fin);
        return new PaginationResult<>(elements, page, size, totalResultats);
    }
}
