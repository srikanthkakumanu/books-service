package com.books.domain.exception;

/** The author still has books and cannot be removed. */
public class AuthorInUseException extends DomainException {

	public AuthorInUseException(String message) {
		super("author-in-use", message);
	}
}
