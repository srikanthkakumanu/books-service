package books.infrastructure.persistence;

import books.domain.model.Author;
import books.domain.model.AuthorQuery;
import books.domain.port.out.AuthorStore;
import books.infrastructure.persistence.repository.AuthorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaAuthorStore implements AuthorStore {
    private final AuthorRepository repository;
    private final AuthorPersistenceMapper mapper;

    public JpaAuthorStore(AuthorRepository repository, AuthorPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override public Author save(Author author) { return mapper.toDomain(repository.save(mapper.toEntity(author))); }
    @Override public Optional<Author> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    @Override public void delete(UUID id) { repository.deleteById(id); }

    @Override public List<Author> find(AuthorQuery query) {
        Specification<books.infrastructure.persistence.entity.Author> filter = (root, criteria, cb) -> cb.conjunction();
        if (query.firstName() != null) { filter = filter.and((root, criteria, cb) -> cb.equal(root.get("firstName"), query.firstName())); }
        if (query.lastName() != null) { filter = filter.and((root, criteria, cb) -> cb.equal(root.get("lastName"), query.lastName())); }
        if (query.genre() != null) { filter = filter.and((root, criteria, cb) -> cb.equal(root.get("genre"), query.genre())); }
        var direction = query.descending() ? Sort.Direction.DESC : Sort.Direction.ASC;
        var sort = query.sorted() ? Sort.by(direction, "firstName", "lastName").and(Sort.by("id")) : Sort.by("id");
        var entities = query.page() == null ? repository.findAll(filter, sort)
                : repository.findAll(filter, PageRequest.of(query.page(), query.size(), sort)).getContent();
        return entities.stream().map(mapper::toDomain).toList();
    }
}
