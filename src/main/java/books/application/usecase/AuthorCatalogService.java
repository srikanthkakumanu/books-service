package books.application.usecase;

import books.domain.model.*;
import books.domain.port.in.AuthorCatalog;
import books.domain.port.out.AuthorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthorCatalogService implements AuthorCatalog {
    private final AuthorStore store;
    public AuthorCatalogService(AuthorStore store) { this.store = store; }

    @Override
    @Transactional
    public Author save(UUID id, AuthorChanges changes, CatalogActor actor) {
        requireManager(actor);
        return store.save(id == null ? Author.create(changes) : findById(id).revise(changes));
    }

    @Override
    @Transactional
    public Author delete(UUID id, CatalogActor actor) {
        requireManager(actor);
        var author = findById(id);
        store.delete(id);
        return author;
    }

    @Override public Author findById(UUID id) {
        return store.findById(id).orElseThrow(() -> new AuthorNotFoundException(id));
    }
    @Override public List<Author> find(AuthorQuery query) { return store.find(query); }

    private void requireManager(CatalogActor actor) {
        if (!actor.managesCatalog()) { throw new CatalogAccessDeniedException("Author changes require ADMIN or MANAGER"); }
    }
}
