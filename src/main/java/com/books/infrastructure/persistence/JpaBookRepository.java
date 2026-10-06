package com.books.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.books.domain.exception.DuplicateBookException;
import com.books.domain.model.AuthorId;
import com.books.domain.model.Book;
import com.books.domain.model.BookDetails;
import com.books.domain.model.BookId;
import com.books.domain.model.BookSearch;
import com.books.domain.model.Isbn;
import com.books.domain.model.OwnerId;
import com.books.domain.model.PageResult;
import com.books.domain.model.Publisher;
import com.books.domain.model.Title;
import com.books.domain.port.BookRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaBookRepository implements BookRepository {

	private final SpringDataBookRepository repository;

	JpaBookRepository(SpringDataBookRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public Book save(Book book) {
		BookDetails details = book.details();
		BookJpaEntity entity = repository.findById(book.id().value())
				.orElseGet(() -> new BookJpaEntity(book.id().value(), book.createdAt()));
		entity.apply(details.title().value(), details.description(), details.isbn().value(),
				details.publisher().value(), details.authorId().value(),
				book.ownerId().map(OwnerId::value).orElse(null), details.completed(), book.updatedAt());
		try {
			return toDomain(repository.saveAndFlush(entity));
		}
		catch (DataIntegrityViolationException ex) {
			// The ISBN was taken between the check and the write; the unique constraint is the last word.
			throw new DuplicateBookException("Another book already has ISBN " + details.isbn());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Book> findById(BookId id) {
		return repository.findById(id.value()).map(JpaBookRepository::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Book> findByIsbn(Isbn isbn) {
		return repository.findByIsbn(isbn.value()).map(JpaBookRepository::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean exists(BookId id) {
		return repository.existsById(id.value());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByAuthor(AuthorId authorId) {
		return repository.existsByAuthorId(authorId.value());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<Book> search(BookSearch search) {
		Specification<BookJpaEntity> matching = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (search.title() != null) {
				predicates.add(Queries.contains(cb, root.get("title"), search.title()));
			}
			if (search.publisher() != null) {
				predicates.add(Queries.contains(cb, root.get("publisher"), search.publisher()));
			}
			if (search.isbn() != null) {
				predicates.add(cb.equal(root.get("isbn"), search.isbn().value()));
			}
			if (search.authorId() != null) {
				predicates.add(cb.equal(root.get("authorId"), search.authorId().value()));
			}
			if (search.ownerId() != null) {
				predicates.add(cb.equal(root.get("ownerId"), search.ownerId().value()));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
		Page<BookJpaEntity> page = repository.findAll(matching, Queries.page(search.paging()));
		return new PageResult<>(page.getContent().stream().map(JpaBookRepository::toDomain).toList(),
				page.getTotalElements(), search.paging().page(), search.paging().size());
	}

	@Override
	@Transactional
	public void delete(BookId id) {
		repository.deleteById(id.value());
	}

	private static Book toDomain(BookJpaEntity entity) {
		BookDetails details = new BookDetails(new Title(entity.getTitle()), entity.getDescription(),
				new Isbn(entity.getIsbn()), new Publisher(entity.getPublisher()), new AuthorId(entity.getAuthorId()),
				entity.isCompleted());
		return Book.rehydrate(new BookId(entity.getId()), details,
				entity.getOwnerId() == null ? null : new OwnerId(entity.getOwnerId()), entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}
