package com.books.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.books.domain.model.Author;
import com.books.domain.model.AuthorId;
import com.books.domain.model.Book;
import com.books.domain.model.BookDetails;
import com.books.domain.model.BookId;
import com.books.domain.model.Genre;
import com.books.domain.model.Isbn;
import com.books.domain.model.PersonName;
import com.books.domain.model.Publisher;
import com.books.domain.model.Title;
import com.books.domain.port.AuthorRepository;
import com.books.domain.port.BookRepository;

/**
 * Loads the starter catalog. Every entry goes through the domain, so seed data obeys the same
 * rules as data entered through the API. Entries whose ID is already stored are left alone, which
 * makes loading safe to repeat and never overwrites later edits.
 */
public class SeedCatalog {

	public record SeedAuthor(String id, String firstName, String lastName, String genre) {
	}

	public record SeedBook(String id, String title, String description, String isbn, String publisher,
			String authorId) {
	}

	public record Result(int authorsAdded, int booksAdded) {
	}

	private final AuthorRepository authors;
	private final BookRepository books;
	private final Clock clock;

	public SeedCatalog(AuthorRepository authors, BookRepository books, Clock clock) {
		this.authors = authors;
		this.books = books;
		this.clock = clock;
	}

	public Result handle(List<SeedAuthor> seedAuthors, List<SeedBook> seedBooks) {
		Instant now = clock.instant();
		int authorsAdded = 0;
		for (SeedAuthor seed : seedAuthors) {
			AuthorId id = AuthorId.of(seed.id());
			if (!authors.exists(id)) {
				authors.save(Author.create(id, new PersonName(seed.firstName(), seed.lastName()),
						new Genre(seed.genre()), now));
				authorsAdded++;
			}
		}
		int booksAdded = 0;
		for (SeedBook seed : seedBooks) {
			BookId id = BookId.of(seed.id());
			if (!books.exists(id)) {
				BookDetails details = new BookDetails(new Title(seed.title()), seed.description(),
						new Isbn(seed.isbn()), new Publisher(seed.publisher()), AuthorId.of(seed.authorId()), false);
				Catalog.authorOf(authors, details);
				books.save(Book.catalogOwned(id, details, now));
				booksAdded++;
			}
		}
		return new Result(authorsAdded, booksAdded);
	}
}
