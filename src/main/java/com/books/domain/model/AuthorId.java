package com.books.domain.model;

import java.util.UUID;

/** Identity of an author. */
public record AuthorId(UUID value) {

	public AuthorId {
		Identifiers.require("authorId", value);
	}

	public static AuthorId of(String value) {
		return new AuthorId(Identifiers.parse("authorId", value));
	}

	public static AuthorId newId() {
		return new AuthorId(UUID.randomUUID());
	}
	@Override
	public String toString() {
		return value.toString();
	}
}
