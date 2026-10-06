package com.books.domain.model;

import java.util.Set;

/** Filters for finding books; a null filter is not applied. Text filters match anywhere, ignoring case. */
public record BookSearch(String title, Isbn isbn, String publisher, AuthorId authorId, OwnerId ownerId, Paging paging) {

	public static final Set<String> SORT_FIELDS = Set.of("title", "publisher", "createdAt");
	public static final String DEFAULT_SORT = "title";

	public BookSearch {
		title = blankToNull(title);
		publisher = blankToNull(publisher);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}
}
