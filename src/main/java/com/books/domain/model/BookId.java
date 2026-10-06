package com.books.domain.model;

import java.util.UUID;

/** Identity of a book. */
public record BookId(UUID value) {

	public BookId {
		Identifiers.require("id", value);
	}

	public static BookId of(String value) {
		return new BookId(Identifiers.parse("id", value));
	}

	public static BookId newId() {
		return new BookId(UUID.randomUUID());
	}
	@Override
	public String toString() {
		return value.toString();
	}
}
