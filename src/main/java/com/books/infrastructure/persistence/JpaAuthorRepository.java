package com.books.infrastructure.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.books.domain.exception.AuthorInUseException;
import com.books.domain.model.Author;
import com.books.domain.model.AuthorId;
import com.books.domain.model.AuthorSearch;
import com.books.domain.model.Genre;
import com.books.domain.model.PageResult;
import com.books.domain.model.PersonName;
import com.books.domain.port.AuthorRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaAuthorRepository implements AuthorRepository {

	private final SpringDataAuthorRepository repository;

	JpaAuthorRepository(SpringDataAuthorRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public Author save(Author author) {
		AuthorJpaEntity entity = repository.findById(author.id().value())
				.orElseGet(() -> new AuthorJpaEntity(author.id().value(), author.createdAt()));
		entity.apply(author.name().firstName(), author.name().lastName(), author.genre().value(), author.updatedAt());
		return toDomain(repository.saveAndFlush(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Author> findById(AuthorId id) {
		return repository.findById(id.value()).map(JpaAuthorRepository::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Author> findByIds(Collection<AuthorId> ids) {
		return repository.findAllById(ids.stream().map(AuthorId::value).toList()).stream()
				.map(JpaAuthorRepository::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean exists(AuthorId id) {
		return repository.existsById(id.value());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<Author> search(AuthorSearch search) {
		Specification<AuthorJpaEntity> matching = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (search.name() != null) {
				predicates.add(cb.or(Queries.contains(cb, root.get("firstName"), search.name()),
						Queries.contains(cb, root.get("lastName"), search.name())));
			}
			if (search.genre() != null) {
				predicates.add(Queries.contains(cb, root.get("genre"), search.genre()));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
		Page<AuthorJpaEntity> page = repository.findAll(matching, Queries.page(search.paging()));
		return new PageResult<>(page.getContent().stream().map(JpaAuthorRepository::toDomain).toList(),
				page.getTotalElements(), search.paging().page(), search.paging().size());
	}

	@Override
	@Transactional
	public void delete(AuthorId id) {
		try {
			repository.deleteById(id.value());
			repository.flush();
		}
		catch (DataIntegrityViolationException ex) {
			// A book was added for this author between the check and the delete.
			throw new AuthorInUseException("Author " + id + " still has books");
		}
	}

	private static Author toDomain(AuthorJpaEntity entity) {
		return Author.rehydrate(new AuthorId(entity.getId()), new PersonName(entity.getFirstName(), entity.getLastName()),
				new Genre(entity.getGenre()), entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
