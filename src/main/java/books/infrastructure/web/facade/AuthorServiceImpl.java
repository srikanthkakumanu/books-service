package books.infrastructure.web.facade;

import books.domain.model.AuthorQuery;
import books.domain.port.in.AuthorCatalog;
import books.infrastructure.web.dto.AuthorDTO;
import books.infrastructure.web.dto.AuthorRequest;
import books.infrastructure.web.dto.SortOrder;
import books.infrastructure.web.mapper.AuthorMapper;
import books.infrastructure.security.CurrentCatalogActor;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.UUID;

@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorCatalog catalog;
    private final AuthorMapper mapper;
    private final CurrentCatalogActor actor;

    public AuthorServiceImpl(AuthorCatalog catalog, AuthorMapper mapper, CurrentCatalogActor actor) {
        this.catalog = catalog;
        this.mapper = mapper;
        this.actor = actor;
    }

    @Override public AuthorDTO save(AuthorRequest dto) { return mapper.toDTO(catalog.save(dto.id(), mapper.toChanges(dto), actor.get())); }
    @Override public Iterable<AuthorDTO> save(Collection<AuthorRequest> authors) { return authors.stream().map(this::save).toList(); }
    @Override public AuthorDTO delete(UUID id) { return mapper.toDTO(catalog.delete(id, actor.get())); }
    @Override public AuthorDTO findById(UUID id) { return mapper.toDTO(catalog.findById(id)); }
    @Override public Iterable<AuthorDTO> findAll() { return find(AuthorQuery.all()); }
    @Override public Iterable<AuthorDTO> findByFirstName(String value) { return find(query(value, null, null, null, 20, false, SortOrder.ASC)); }
    @Override public Iterable<AuthorDTO> findByLastName(String value) { return find(query(null, value, null, null, 20, false, SortOrder.ASC)); }
    @Override public Iterable<AuthorDTO> findByFirstNameAndLastName(String first, String last) { return find(query(first, last, null, null, 20, false, SortOrder.ASC)); }
    @Override public Iterable<AuthorDTO> findByGenre(String value) { return find(query(null, null, value, null, 20, false, SortOrder.ASC)); }
    @Override public Iterable<AuthorDTO> findAll(int page, int size) { return find(query(null, null, null, page, size, false, SortOrder.ASC)); }
    @Override public Iterable<AuthorDTO> findByFirstNameAndLastName(String first, String last, int page, int size, Boolean sorted, SortOrder order) {
        return find(query(first, last, null, page, size, sorted, order));
    }
    @Override public Iterable<AuthorDTO> findByFirstName(String value, int page, int size, Boolean sorted, SortOrder order) {
        return find(query(value, null, null, page, size, sorted, order));
    }
    @Override public Iterable<AuthorDTO> findByLastName(String value, int page, int size, Boolean sorted, SortOrder order) {
        return find(query(null, value, null, page, size, sorted, order));
    }
    @Override public Iterable<AuthorDTO> findByGenre(String value, int page, int size, Boolean sorted, SortOrder order) {
        return find(query(null, null, value, page, size, sorted, order));
    }
    private AuthorQuery query(String first, String last, String genre, Integer page, int size, Boolean sorted, SortOrder order) {
        return new AuthorQuery(first, last, genre, page, size, Boolean.TRUE.equals(sorted), order == SortOrder.DSC);
    }
    private Iterable<AuthorDTO> find(AuthorQuery query) { return catalog.find(query).stream().map(mapper::toDTO).toList(); }
}
