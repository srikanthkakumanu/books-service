package books.domain.model;

import java.util.UUID;

public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(UUID id) { super("Author %s does not exist".formatted(id)); }
}
