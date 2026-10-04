package books.domain.port.in;

import books.domain.model.Author;
import books.domain.model.AuthorChanges;
import books.domain.model.AuthorQuery;
import books.domain.model.CatalogActor;
import java.util.List;
import java.util.UUID;

public interface AuthorCatalog {
    Author save(UUID id, AuthorChanges changes, CatalogActor actor);
    Author delete(UUID id, CatalogActor actor);
    Author findById(UUID id);
    List<Author> find(AuthorQuery query);
}
