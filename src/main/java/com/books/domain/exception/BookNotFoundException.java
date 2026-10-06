package com.books.domain.exception;

/** No book has the requested ID. */
public class BookNotFoundException extends DomainException {

	public BookNotFoundException(String message) {
		super("book-not-found", message);
	}
}
