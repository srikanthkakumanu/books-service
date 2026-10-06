package com.books.domain.model;

import java.util.Set;

/** Filters for finding authors; a null filter is not applied. Text filters match anywhere, ignoring case. */
public record AuthorSearch(String name, String genre, Paging paging) {

	public static final Set<String> SORT_FIELDS = Set.of("lastName", "firstName", "genre", "createdAt");
	public static final String DEFAULT_SORT = "lastName";

	public AuthorSearch {
		name = blankToNull(name);
		genre = blankToNull(genre);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}
}
