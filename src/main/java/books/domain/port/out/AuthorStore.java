package books.domain.port.out;

import books.domain.model.Author;
import books.domain.model.AuthorQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthorStore {
    Author save(Author author);
    Optional<Author> findById(UUID id);
    void delete(UUID id);
    List<Author> find(AuthorQuery query);
}
