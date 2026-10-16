package ma.ali.mahraoui.recherche;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import ma.ali.mahraoui.recherche.model.Reservation;
import ma.ali.mahraoui.recherche.model.Salle;
import ma.ali.mahraoui.recherche.repository.SalleRepositoryImpl;
import ma.ali.mahraoui.recherche.service.SalleService;
import ma.ali.mahraoui.recherche.service.SalleServiceImpl;
import ma.ali.mahraoui.recherche.util.PaginationResult;
import org.h2.tools.Server;

/**
 * Point d'entrée principal de l'application de gestion et de recherche avancée de salles.
 * Initialise le contexte JPA, injecte un jeu d'essai réaliste et exécute divers scénarios de démonstration.
 *
 * @author Ali Mahraoui
 */
public class ApplicationRecherche {

    private static final DateTimeFormatter HEURE_FORMATTER = DateTimeFormatter.ofPattern("HH'h'mm");

    public static void main(String[] args) throws Exception {
        imprimerBanniere();

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        try {
            EntityManager em = emf.createEntityManager();
            try {
                LocalDateTime debutReference = LocalDateTime.of(2026, 11, 16, 9, 0);
                initialiserJeuDeDonnees(em, debutReference);

                em.clear();
                SalleService service = new SalleServiceImpl(new SalleRepositoryImpl(em));

                System.out.println("\n------------------------------------------------------------");
                System.out.println(" 1. SALLES DISPONIBLES DE 09h00 A 11h00");
                System.out.println("------------------------------------------------------------");
                List<Salle> disponibles = service.getAvailableRooms(debutReference, debutReference.plusHours(2));
                disponibles.forEach(s -> System.out.println("  -> " + s));

                System.out.println("\n------------------------------------------------------------");
                System.out.println(" 2. RECHERCHE COMBINÉE : Nom='a', Capacité>=20, Créneau 9h-11h");
                System.out.println("------------------------------------------------------------");
                PaginationResult<Salle> pageFiltree = service.searchPage("a", 20, debutReference, debutReference.plusHours(2), 1, 2);
                afficherPage(pageFiltree);

                System.out.println("\n------------------------------------------------------------");
                System.out.println(" 3. PARCOURS PAGINÉ COMPLET (Taille = 2 salles/page)");
                System.out.println("------------------------------------------------------------");
                for (int pageIndex = 1; pageIndex <= 3; pageIndex++) {
                    afficherPage(service.searchPage(null, null, null, null, pageIndex, 2));
                }

                System.out.println("\n------------------------------------------------------------");
                System.out.println(" 4. ACCÈS HORS LIMITES (Page 4 sur 3)");
                System.out.println("------------------------------------------------------------");
                afficherPage(service.searchPage(null, null, null, null, 4, 2));

                if (Arrays.asList(args).contains("--console")) {
                    Server webServer = Server.createWebServer("-webPort", "8082").start();
                    try {
                        System.out.println("\n============================================================");
                        System.out.println(" Console Web H2 démarrée : http://localhost:8082");
                        System.out.println(" URL JDBC : jdbc:h2:mem:recherche | User : sa | Mot de passe : (vide)");
                        System.out.println(" Appuyez sur Entrée pour arrêter le serveur...");
                        System.out.println("============================================================");
                        System.in.read();
                    } finally {
                        webServer.stop();
                    }
                }
            } finally {
                em.close();
            }
        } finally {
            emf.close();
        }
    }

    private static void initialiserJeuDeDonnees(EntityManager em, LocalDateTime debut) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Salle atlas = new Salle("Atlas", 30);
            Salle menara = new Salle("Menara", 20);
            Salle toubkal = new Salle("Toubkal", 50);
            Salle ourika = new Salle("Ourika", 12);
            Salle agdal = new Salle("Agdal", 25);

            new Reservation(atlas, debut, debut.plusHours(2));
            new Reservation(menara, debut.plusHours(2), debut.plusHours(3));
            new Reservation(toubkal, debut.plusMinutes(30), debut.plusHours(1));

            for (Salle salle : Arrays.asList(atlas, menara, toubkal, ourika, agdal)) {
                em.persist(salle);
            }
            tx.commit();
            System.out.println("[INFO] Jeu de données initialisé avec succès dans la base H2.");
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    private static void afficherPage(PaginationResult<Salle> page) {
        System.out.printf("  [Page %d/%d] Total éléments : %d | Éléments affichés : %d%n",
                page.getCurrentPage(), page.getTotalPages(), page.getTotalItems(), page.getItems().size());
        if (page.isEmpty()) {
            System.out.println("   (Aucun résultat pour cette sélection)");
        } else {
            page.getItems().forEach(salle -> System.out.println("    * " + salle));
        }
    }

    private static void imprimerBanniere() {
        System.out.println("============================================================");
        System.out.println("        SYSTÈME DE GESTION ET RECHERCHE DE SALLES          ");
        System.out.println("                Développé par : Ali Mahraoui                ");
        System.out.println("============================================================");
    }
}
