package com.books.application;

import java.time.Clock;

import com.books.domain.model.Book;
import com.books.domain.model.BookId;
import com.books.domain.model.CatalogActor;
import com.books.domain.model.OwnerId;
import com.books.domain.port.AuthorRepository;
import com.books.domain.port.BookRepository;
import com.books.domain.port.UserDirectoryPort;

/** Gives a book to another user. Only a catalog manager may, and only to an active platform user. */
public class TransferBook {

	private final BookRepository books;
	private final AuthorRepository authors;
	private final UserDirectoryPort directory;
	private final Clock clock;

	public TransferBook(BookRepository books, AuthorRepository authors, UserDirectoryPort directory, Clock clock) {
		this.books = books;
		this.authors = authors;
		this.directory = directory;
		this.clock = clock;
	}

	public BookView handle(BookId id, OwnerId newOwner, CatalogActor actor) {
		Book transferred = Catalog.book(books, id).transferTo(newOwner, actor, clock.instant());
		Catalog.requireEligibleOwner(directory, newOwner);
		Book saved = books.save(transferred);
		return new BookView(saved, Catalog.author(authors, saved.details().authorId()));
	}
}
