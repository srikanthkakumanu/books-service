package com.books.domain.exception;

/** A catalog rule refuses the operation, whatever permissions the caller holds. */
public class OperationNotPermittedException extends DomainException {

	public OperationNotPermittedException(String message) {
		super("operation-not-permitted", message);
	}
}
