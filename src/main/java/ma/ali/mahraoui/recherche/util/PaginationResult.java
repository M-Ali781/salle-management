package ma.ali.mahraoui.recherche.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsule les résultats d'une requête paginée avec ses métadonnées de navigation.
 * Structure immuable garantissant l'intégrité des données retournées.
 *
 * @param <T> type des éléments paginés
 * @author Ali Mahraoui
 */
public final class PaginationResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final long totalItems;
    private final long totalPages;

    /**
     * Construit une page de résultats avec contrôle de validité des index.
     *
     * @param items       liste des éléments de la page actuelle
     * @param currentPage numéro de la page demandée (1-indexé)
     * @param pageSize    taille maximale de la page
     * @param totalItems  nombre total d'éléments correspondant aux filtres
     */
    public PaginationResult(List<T> items, int currentPage, int pageSize, long totalItems) {
        ValidationRecherche.offset(currentPage, pageSize);
        if (totalItems < 0) {
            throw new IllegalArgumentException("Le total ne peut pas être négatif.");
        }

        this.items = (items != null) ? Collections.unmodifiableList(new ArrayList<>(items)) : Collections.emptyList();
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (totalItems == 0) ? 0 : ((totalItems + pageSize - 1) / pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public boolean hasPrevious() {
        return currentPage > 1;
    }

    public boolean hasNext() {
        return currentPage < totalPages;
    }

    public boolean isFirst() {
        return currentPage == 1;
    }

    public boolean isLast() {
        return currentPage >= totalPages;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("PaginationResult[page=%d/%d, total=%d, taille=%d, nbElements=%d]",
                currentPage, totalPages, totalItems, pageSize, items.size());
    }
}
