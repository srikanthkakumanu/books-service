package com.books.application;

import com.books.domain.exception.AuthorInUseException;
import com.books.domain.model.AuthorId;
import com.books.domain.port.AuthorRepository;
import com.books.domain.port.BookRepository;

/** Removes an author, which is only possible once no book names them. */
public class DeleteAuthor {

	private final AuthorRepository authors;
	private final BookRepository books;

	public DeleteAuthor(AuthorRepository authors, BookRepository books) {
		this.authors = authors;
		this.books = books;
	}

	public void handle(AuthorId id) {
		Catalog.author(authors, id);
		if (books.existsByAuthor(id)) {
			throw new AuthorInUseException("Author " + id + " still has books");
		}
		authors.delete(id);
	}
}
