package com.books.application;

import com.books.domain.model.BookId;
import com.books.domain.model.CatalogActor;
import com.books.domain.port.BookRepository;

/** Removes a book. Only its owner or a catalog manager may. */
public class DeleteBook {

	private final BookRepository books;

	public DeleteBook(BookRepository books) {
		this.books = books;
	}

	public void handle(BookId id, CatalogActor actor) {
		Catalog.book(books, id).requireWriteAccess(actor);
		books.delete(id);
	}
}
