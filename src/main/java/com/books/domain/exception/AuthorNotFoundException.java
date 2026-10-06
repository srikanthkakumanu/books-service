package com.books.domain.exception;

/** No author has the requested ID. */
public class AuthorNotFoundException extends DomainException {

	public AuthorNotFoundException(String message) {
		super("author-not-found", message);
	}
}
