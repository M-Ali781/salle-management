package ma.ali.mahraoui.recherche;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import ma.ali.mahraoui.recherche.model.Reservation;
import ma.ali.mahraoui.recherche.model.Salle;
import ma.ali.mahraoui.recherche.repository.SalleRepositoryImpl;
import ma.ali.mahraoui.recherche.service.SalleService;
import ma.ali.mahraoui.recherche.service.SalleServiceImpl;
import ma.ali.mahraoui.recherche.util.PaginationResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Suite de tests unitaires et d'intégration validant le moteur de recherche et de réservation.
 * Vérifie la précision des requêtes JPQL, Criteria API, la gestion de pagination et le cycle de vie JPA.
 *
 * @author Ali Mahraoui
 */
public class RechercheSallesTest {

    private EntityManagerFactory emf;
    private EntityManager em;
    private SalleService salleService;
    private final LocalDateTime dateDebutRef = LocalDateTime.of(2026, 11, 16, 9, 0);

    @Before
    public void setUp() {
        Map<String, Object> proprietes = new HashMap<>();
        proprietes.put("javax.persistence.jdbc.url", "jdbc:h2:mem:test_db_" + UUID.randomUUID());
        proprietes.put("hibernate.hbm2ddl.auto", "create-drop");
        proprietes.put("hibernate.show_sql", "false");

        emf = Persistence.createEntityManagerFactory("SallePU", proprietes);
        em = emf.createEntityManager();
        salleService = new SalleServiceImpl(new SalleRepositoryImpl(em));

        em.getTransaction().begin();
        Salle atlas = new Salle("Atlas", 30);
        Salle menara = new Salle("Menara", 20);
        Salle toubkal = new Salle("Toubkal", 50);
        Salle ourika = new Salle("Ourika", 12);
        Salle agdal = new Salle("Agdal", 25);

        new Reservation(atlas, dateDebutRef, dateDebutRef.plusHours(2));
        new Reservation(atlas, dateDebutRef.plusMinutes(15), dateDebutRef.plusMinutes(45));
        new Reservation(menara, dateDebutRef.plusHours(2), dateDebutRef.plusHours(3));
        new Reservation(toubkal, dateDebutRef.plusMinutes(30), dateDebutRef.plusHours(1));

        for (Salle s : Arrays.asList(atlas, menara, toubkal, ourika, agdal)) {
            em.persist(s);
        }
        em.getTransaction().commit();
        em.clear();
    }

    @After
    public void tearDown() {
        if (em != null && em.isOpen()) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private List<String> extraireNoms(List<Salle> liste) {
        return liste.stream().map(Salle::getNom).collect(Collectors.toList());
    }

    @Test
    public void disponibiliteJpqlAvecChevauchementsEtSallesSansReservation() {
        assertEquals(Arrays.asList("Agdal", "Menara", "Ourika"),
                extraireNoms(salleService.getAvailableRooms(dateDebutRef, dateDebutRef.plusHours(2))));
        assertEquals(Arrays.asList("Agdal", "Menara", "Ourika"),
                extraireNoms(salleService.getAvailableRooms(dateDebutRef.plusMinutes(40), dateDebutRef.plusMinutes(50))));
    }

    @Test
    public void creneauxAdjacentsSontDisponibles() {
        assertEquals(5, salleService.getAvailableRooms(dateDebutRef.minusHours(1), dateDebutRef).size());
        assertEquals(Arrays.asList("Agdal", "Atlas", "Ourika", "Toubkal"),
                extraireNoms(salleService.getAvailableRooms(dateDebutRef.plusHours(2), dateDebutRef.plusHours(3))));
    }

    @Test
    public void requetesJpqlEtCriteriaDonnentLesMemesResultats() {
        assertEquals(extraireNoms(salleService.getAvailableRooms(dateDebutRef, dateDebutRef.plusHours(2))),
                extraireNoms(salleService.searchRooms(null, null, dateDebutRef, dateDebutRef.plusHours(2), 1, 10)));
        assertEquals(3, salleService.countRooms(null, null, dateDebutRef, dateDebutRef.plusHours(2)));
    }

    @Test
    public void combinerTousLesFiltresEtCompterAvecLaBonneRacine() {
        PaginationResult<Salle> page = salleService.searchPage(" A ", 20, dateDebutRef, dateDebutRef.plusHours(2), 2, 1);
        assertEquals(Collections.singletonList("Menara"), extraireNoms(page.getItems()));
        assertEquals(2, page.getTotalItems());
        assertEquals(2, page.getTotalPages());
        assertEquals(2, page.getCurrentPage());
        assertEquals(1, page.getPageSize());
    }

    @Test
    public void filtrerNomEtCapaciteIndependamment() {
        assertEquals(Collections.singletonList("Atlas"),
                extraireNoms(salleService.searchRooms("ATLAS", null, null, null, 1, 10)));
        assertEquals(Arrays.asList("Agdal", "Atlas", "Menara", "Toubkal"),
                extraireNoms(salleService.searchRooms(null, 20, null, null, 1, 10)));
        assertEquals(5, salleService.countRooms("   ", null, null, null));
        assertEquals(4, salleService.countRooms(null, 20, null, null));
    }

    @Test
    public void paginationStableSansDoublonEtDernierePagePartielle() {
        List<String> toutesLesSalles = new ArrayList<>();
        for (int p = 1; p <= 3; p++) {
            PaginationResult<Salle> page = salleService.searchPage(null, null, null, null, p, 2);
            assertEquals(5, page.getTotalItems());
            assertEquals(3, page.getTotalPages());
            assertEquals(p == 3 ? 1 : 2, page.getItems().size());
            toutesLesSalles.addAll(extraireNoms(page.getItems()));
        }
        assertEquals(Arrays.asList("Agdal", "Atlas", "Menara", "Ourika", "Toubkal"), toutesLesSalles);
    }

    @Test
    public void gererResultatsVidesEtPageHorsLimites() {
        PaginationResult<Salle> vide = salleService.searchPage("Inconnue", null, null, null, 1, 2);
        assertTrue(vide.getItems().isEmpty());
        assertEquals(0, vide.getTotalItems());
        assertEquals(0, vide.getTotalPages());

        PaginationResult<Salle> horsLimites = salleService.searchPage(null, null, null, null, 4, 2);
        assertTrue(horsLimites.getItems().isEmpty());
        assertEquals(5, horsLimites.getTotalItems());
        assertEquals(3, horsLimites.getTotalPages());
    }

    @Test
    public void refuserDatesEtPaginationInvalides() {
        assertThrows(IllegalArgumentException.class, () -> salleService.getAvailableRooms(null, dateDebutRef));
        assertThrows(IllegalArgumentException.class, () -> salleService.getAvailableRooms(dateDebutRef, dateDebutRef));
        assertThrows(IllegalArgumentException.class, () -> salleService.getAvailableRooms(dateDebutRef, dateDebutRef.minusHours(1)));
        assertThrows(IllegalArgumentException.class, () -> salleService.countRooms(null, null, dateDebutRef, null));
        assertThrows(IllegalArgumentException.class, () -> salleService.countRooms(null, -1, null, null));
        assertThrows(IllegalArgumentException.class, () -> salleService.searchRooms(null, null, null, null, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> salleService.searchRooms(null, null, null, null, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> salleService.searchRooms(null, null, null, null, Integer.MAX_VALUE, 2));
    }

    @Test
    public void rechercherCaracteresSpeciauxCommeTexteLitteral() {
        em.getTransaction().begin();
        em.persist(new Salle("Studio 100%_!", 8));
        em.getTransaction().commit();
        em.clear();

        for (String motif : Arrays.asList("%", "_", "!", "100%_!")) {
            assertEquals(Collections.singletonList("Studio 100%_!"),
                    extraireNoms(salleService.searchRooms(motif, null, null, null, 1, 10)));
            assertEquals(1, salleService.countRooms(motif, null, null, null));
        }
    }

    @Test
    public void verifierCascadeEtRelationPersistante() {
        Salle atlas = salleService.searchRooms("Atlas", null, null, null, 1, 10).get(0);
        assertEquals(2, atlas.getReservations().size());
        for (Reservation reservation : atlas.getReservations()) {
            assertEquals(atlas.getId(), reservation.getSalle().getId());
        }
        assertThrows(IllegalArgumentException.class, () -> new Reservation(atlas, dateDebutRef, dateDebutRef));

        PaginationResult<Salle> page = salleService.searchPage(null, null, null, null, 1, 2);
        assertThrows(UnsupportedOperationException.class, () -> page.getItems().clear());
    }
}
