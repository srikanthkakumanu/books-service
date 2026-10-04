package books.domain.model;

import java.util.UUID;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(UUID id) {
        super("Book %s does not exist".formatted(id));
    }
}
