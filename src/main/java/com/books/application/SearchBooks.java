package com.books.application;

import com.books.domain.model.Book;
import com.books.domain.model.BookSearch;
import com.books.domain.model.PageResult;
import com.books.domain.port.AuthorRepository;
import com.books.domain.port.BookRepository;

public class SearchBooks {

	private final BookRepository books;
	private final AuthorRepository authors;

	public SearchBooks(BookRepository books, AuthorRepository authors) {
		this.books = books;
		this.authors = authors;
	}

	public PageResult<BookView> handle(BookSearch search) {
		PageResult<Book> page = books.search(search);
		return new PageResult<>(Catalog.withAuthors(authors, page.items()), page.total(), page.page(), page.size());
	}
}
