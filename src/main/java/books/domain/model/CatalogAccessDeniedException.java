package books.domain.model;

public class CatalogAccessDeniedException extends RuntimeException {
    public CatalogAccessDeniedException(String message) {
        super(message);
    }
}
