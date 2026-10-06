package com.books.application;

import java.time.Clock;

import com.books.domain.model.Author;
import com.books.domain.model.Book;
import com.books.domain.model.BookDetails;
import com.books.domain.model.BookId;
import com.books.domain.model.CatalogActor;
import com.books.domain.port.AuthorRepository;
import com.books.domain.port.BookRepository;

/** Replaces the details of a book. Only its owner or a catalog manager may. */
public class UpdateBook {

	private final BookRepository books;
	private final AuthorRepository authors;
	private final Clock clock;

	public UpdateBook(BookRepository books, AuthorRepository authors, Clock clock) {
		this.books = books;
		this.authors = authors;
		this.clock = clock;
	}

	public BookView handle(BookId id, BookDetails details, CatalogActor actor) {
		Book revised = Catalog.book(books, id).revise(details, actor, clock.instant());
		Author author = Catalog.authorOf(authors, details);
		Catalog.requireIsbnFree(books, details, id);
		return new BookView(books.save(revised), author);
	}
}
