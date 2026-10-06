package com.books.application;

import com.books.domain.model.Author;
import com.books.domain.model.AuthorId;
import com.books.domain.port.AuthorRepository;

public class GetAuthor {

	private final AuthorRepository authors;

	public GetAuthor(AuthorRepository authors) {
		this.authors = authors;
	}

	public Author handle(AuthorId id) {
		return Catalog.author(authors, id);
	}
}
