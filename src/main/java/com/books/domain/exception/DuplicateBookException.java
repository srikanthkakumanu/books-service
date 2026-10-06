package com.books.domain.exception;

/** Another book already has this ISBN. */
public class DuplicateBookException extends DomainException {

	public DuplicateBookException(String message) {
		super("duplicate-book", message);
	}
}
