package ma.ali.mahraoui.recherche.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import ma.ali.mahraoui.recherche.model.Reservation;
import ma.ali.mahraoui.recherche.model.Salle;
import ma.ali.mahraoui.recherche.util.ValidationRecherche;

/**
 * Implémentation du référentiel {@link SalleRepository} exploitant JPA Criteria API et JPQL.
 * Garantit l'absence d'injections et une gestion optimisée des requêtes de disponibilité.
 *
 * @author Ali Mahraoui
 */
public class SalleRepositoryImpl implements SalleRepository {

    private static final char CARACTERE_ECHAPPEMENT = '!';

    private static final String REQUETE_JPQL_DISPONIBILITE = 
            "SELECT s FROM Salle s " +
            "WHERE s.id NOT IN (" +
            "   SELECT r.salle.id FROM Reservation r " +
            "   WHERE :debut < r.fin AND :fin > r.debut" +
            ") " +
            "ORDER BY s.nom ASC, s.id ASC";

    private final EntityManager entityManager;

    /**
     * Initialise le repository avec l'EntityManager fourni.
     *
     * @param entityManager gestionnaire d'entités JPA
     */
    public SalleRepositoryImpl(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "L'EntityManager ne doit pas être null.");
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        ValidationRecherche.creneau(debut, fin, true);

        TypedQuery<Salle> query = entityManager.createQuery(REQUETE_JPQL_DISPONIBILITE, Salle.class);
        query.setParameter("debut", debut);
        query.setParameter("fin", fin);

        return query.getResultList();
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size) {
        ValidationRecherche.filtres(capaciteMin, debut, fin);
        int positionDepart = ValidationRecherche.offset(page, size);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> criteria = builder.createQuery(Salle.class);
        Root<Salle> salleRoot = criteria.from(Salle.class);

        Predicate[] filtres = elaborerFiltres(builder, criteria, salleRoot, nom, capaciteMin, debut, fin);

        criteria.select(salleRoot)
                .where(filtres)
                .orderBy(builder.asc(salleRoot.get("nom")), builder.asc(salleRoot.get("id")));

        return entityManager.createQuery(criteria)
                .setFirstResult(positionDepart)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        ValidationRecherche.filtres(capaciteMin, debut, fin);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
        Root<Salle> salleRoot = countCriteria.from(Salle.class);

        Predicate[] filtres = elaborerFiltres(builder, countCriteria, salleRoot, nom, capaciteMin, debut, fin);

        countCriteria.select(builder.count(salleRoot)).where(filtres);

        return entityManager.createQuery(countCriteria).getSingleResult();
    }

    /**
     * Élabore dynamiquement le tableau de prédicats de filtrage selon les critères fournis.
     */
    private Predicate[] elaborerFiltres(CriteriaBuilder cb, CriteriaQuery<?> query, Root<Salle> salleRoot,
                                        String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        List<Predicate> listePredicats = new ArrayList<>();

        if (nom != null && !nom.trim().isEmpty()) {
            String motifRecherche = "%" + echapperCaracteresGeneriques(nom) + "%";
            listePredicats.add(cb.like(cb.lower(salleRoot.get("nom")), motifRecherche, CARACTERE_ECHAPPEMENT));
        }

        if (capaciteMin != null) {
            listePredicats.add(cb.greaterThanOrEqualTo(salleRoot.get("capacite"), capaciteMin));
        }

        if (debut != null) {
            Subquery<Long> sousRequeteOccupees = query.subquery(Long.class);
            Root<Reservation> reservationRoot = sousRequeteOccupees.from(Reservation.class);

            sousRequeteOccupees.select(reservationRoot.get("salle").get("id")).where(
                    cb.lessThan(reservationRoot.<LocalDateTime>get("debut"), fin),
                    cb.greaterThan(reservationRoot.<LocalDateTime>get("fin"), debut)
            );

            listePredicats.add(cb.not(salleRoot.get("id").in(sousRequeteOccupees)));
        }

        return listePredicats.toArray(new Predicate[0]);
    }

    /**
     * Échappe les méta-caractères SQL (%, _) et le caractère d'échappement lui-même.
     */
    private String echapperCaracteresGeneriques(String valeur) {
        return valeur.trim()
                .toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}
