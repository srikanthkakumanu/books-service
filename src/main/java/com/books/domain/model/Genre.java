package com.books.domain.model;

/** The genre an author writes in. */
public record Genre(String value) {

	public static final int MAX_LENGTH = 100;

	public Genre {
		value = Text.required("genre", value, MAX_LENGTH);
	}

	@Override
	public String toString() {
		return value;
	}
}
