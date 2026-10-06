package com.books.domain.model;

import com.books.domain.exception.InvalidValueException;

/**
 * Whoever is asking for a catalog operation, as far as the catalog's rules need to know: the
 * platform user, and whether auth-service has granted them management of every book.
 */
public record CatalogActor(OwnerId userId, boolean managesAllBooks) {

	public CatalogActor {
		if (userId == null) {
			throw new InvalidValueException("userId", "must not be null");
		}
	}

	public boolean is(OwnerId other) {
		return userId.equals(other);
	}
}
