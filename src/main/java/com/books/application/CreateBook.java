package com.books.application;

import java.time.Clock;

import com.books.domain.model.Author;
import com.books.domain.model.Book;
import com.books.domain.model.BookDetails;
import com.books.domain.model.BookId;
import com.books.domain.model.CatalogActor;
import com.books.domain.model.OwnerId;
import com.books.domain.port.AuthorRepository;
import com.books.domain.port.BookRepository;
import com.books.domain.port.UserDirectoryPort;

/** Adds a book. It belongs to whoever adds it, unless a catalog manager adds it for another user. */
public class CreateBook {

	/** {@code ownerId} is null when the caller adds the book for themselves. */
	public record Command(BookDetails details, OwnerId ownerId) {
	}

	private final BookRepository books;
	private final AuthorRepository authors;
	private final UserDirectoryPort directory;
	private final Clock clock;

	public CreateBook(BookRepository books, AuthorRepository authors, UserDirectoryPort directory, Clock clock) {
		this.books = books;
		this.authors = authors;
		this.directory = directory;
		this.clock = clock;
	}

	public BookView handle(Command command, CatalogActor actor) {
		Book book = Book.create(BookId.newId(), command.details(), command.ownerId(), actor, clock.instant());
		Author author = Catalog.authorOf(authors, command.details());
		Catalog.requireIsbnFree(books, command.details(), book.id());
		if (!book.isOwnedBy(actor.userId())) {
			Catalog.requireEligibleOwner(directory, book.ownerId().orElseThrow());
		}
		return new BookView(books.save(book), author);
	}
}
