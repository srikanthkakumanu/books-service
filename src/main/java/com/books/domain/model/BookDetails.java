package com.books.domain.model;

import com.books.domain.exception.InvalidValueException;

/** Everything about a book that its owner may change. */
public record BookDetails(Title title, String description, Isbn isbn, Publisher publisher, AuthorId authorId,
		boolean completed) {

	public static final int MAX_DESCRIPTION_LENGTH = 1000;

	public BookDetails {
		require("title", title);
		require("isbn", isbn);
		require("publisher", publisher);
		require("authorId", authorId);
		description = Text.optional("description", description, MAX_DESCRIPTION_LENGTH);
	}

	private static void require(String field, Object value) {
		if (value == null) {
			throw new InvalidValueException(field, "must not be null");
		}
	}
}
