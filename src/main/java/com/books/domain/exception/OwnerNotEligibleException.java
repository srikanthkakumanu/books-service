package com.books.domain.exception;

/** The user a book is to be given to does not exist or is not active. */
public class OwnerNotEligibleException extends DomainException {

	public OwnerNotEligibleException(String message) {
		super("owner-not-eligible", message);
	}
}
