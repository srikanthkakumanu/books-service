package com.books.application;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.books.domain.model.Author;
import com.books.domain.model.AuthorId;
import com.books.domain.model.AuthorSearch;
import com.books.domain.model.Book;
import com.books.domain.model.BookId;
import com.books.domain.model.BookSearch;
import com.books.domain.model.Isbn;
import com.books.domain.model.OwnerId;
import com.books.domain.model.PageResult;
import com.books.domain.model.UserStanding;
import com.books.domain.port.AuthorRepository;
import com.books.domain.port.BookRepository;
import com.books.domain.port.UserDirectoryPort;

/** In-memory ports for use-case tests. */
final class InMemoryCatalog {

	private InMemoryCatalog() {
	}

	static final class Books implements BookRepository {

		final Map<BookId, Book> stored = new LinkedHashMap<>();

		@Override
		public Book save(Book book) {
			stored.put(book.id(), book);
			return book;
		}

		@Override
		public Optional<Book> findById(BookId id) {
			return Optional.ofNullable(stored.get(id));
		}

		@Override
		public Optional<Book> findByIsbn(Isbn isbn) {
			return stored.values().stream().filter(book -> book.details().isbn().equals(isbn)).findFirst();
		}

		@Override
		public boolean exists(BookId id) {
			return stored.containsKey(id);
		}

		@Override
		public boolean existsByAuthor(AuthorId authorId) {
			return stored.values().stream().anyMatch(book -> book.details().authorId().equals(authorId));
		}

		@Override
		public PageResult<Book> search(BookSearch search) {
			List<Book> matches = stored.values().stream()
					.filter(book -> search.ownerId() == null || book.isOwnedBy(search.ownerId()))
					.sorted(Comparator.comparing(book -> book.details().title().value()))
					.toList();
			return new PageResult<>(matches, matches.size(), search.paging().page(), search.paging().size());
		}

		@Override
		public void delete(BookId id) {
			stored.remove(id);
		}
	}

	static final class Authors implements AuthorRepository {

		final Map<AuthorId, Author> stored = new LinkedHashMap<>();

		@Override
		public Author save(Author author) {
			stored.put(author.id(), author);
			return author;
		}

		@Override
		public Optional<Author> findById(AuthorId id) {
			return Optional.ofNullable(stored.get(id));
		}

		@Override
		public List<Author> findByIds(Collection<AuthorId> ids) {
			return ids.stream().map(stored::get).filter(java.util.Objects::nonNull).toList();
		}

		@Override
		public boolean exists(AuthorId id) {
			return stored.containsKey(id);
		}

		@Override
		public PageResult<Author> search(AuthorSearch search) {
			List<Author> matches = List.copyOf(stored.values());
			return new PageResult<>(matches, matches.size(), search.paging().page(), search.paging().size());
		}

		@Override
		public void delete(AuthorId id) {
			stored.remove(id);
		}
	}

	/** Knows the users it was told about; everyone else is unknown. Counts how often it is asked. */
	static final class Directory implements UserDirectoryPort {

		final Map<OwnerId, UserStanding> users = new LinkedHashMap<>();
		int lookups;

		@Override
		public UserStanding standingOf(OwnerId userId) {
			lookups++;
			return users.getOrDefault(userId, UserStanding.UNKNOWN);
		}
	}
}
