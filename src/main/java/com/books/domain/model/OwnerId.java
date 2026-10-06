package com.books.domain.model;

import java.util.UUID;

/** The platform user who owns a book: the user ID that user-service manages and tokens carry as subject. */
public record OwnerId(UUID value) {

	public OwnerId {
		Identifiers.require("ownerId", value);
	}

	public static OwnerId of(String value) {
		return new OwnerId(Identifiers.parse("ownerId", value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
