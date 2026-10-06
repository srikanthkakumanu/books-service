package com.books.application;

import com.books.domain.model.Author;
import com.books.domain.model.AuthorSearch;
import com.books.domain.model.PageResult;
import com.books.domain.port.AuthorRepository;

public class SearchAuthors {

	private final AuthorRepository authors;

	public SearchAuthors(AuthorRepository authors) {
		this.authors = authors;
	}

	public PageResult<Author> handle(AuthorSearch search) {
		return authors.search(search);
	}
}
