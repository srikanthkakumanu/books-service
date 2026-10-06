package com.books.domain.model;

/** The house that published a book. */
public record Publisher(String value) {

	public static final int MAX_LENGTH = 200;

	public Publisher {
		value = Text.required("publisher", value, MAX_LENGTH);
	}

	@Override
	public String toString() {
		return value;
	}
}
