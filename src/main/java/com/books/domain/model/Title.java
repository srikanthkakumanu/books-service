package com.books.domain.model;

/** The title of a book. */
public record Title(String value) {

	public static final int MAX_LENGTH = 200;

	public Title {
		value = Text.required("title", value, MAX_LENGTH);
	}

	@Override
	public String toString() {
		return value;
	}
}
