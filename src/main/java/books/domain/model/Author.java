package books.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Author(UUID id, Instant created, Instant updated, String firstName, String lastName, String genre) {
    public Author {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("Author first name is required");
        }
        firstName = firstName.strip();
        if (firstName.length() > 255 || (lastName != null && lastName.length() > 255)
                || (genre != null && genre.length() > 255)) {
            throw new IllegalArgumentException("Author fields must not exceed 255 characters");
        }
    }

    public Author revise(AuthorChanges changes) {
        return new Author(id, created, updated,
                changes.firstName() == null ? firstName : changes.firstName(),
                changes.lastName() == null ? lastName : changes.lastName(),
                changes.genre() == null ? genre : changes.genre());
    }

    public static Author create(AuthorChanges changes) {
        return new Author(null, null, null, changes.firstName(), changes.lastName(), changes.genre());
    }
}
