package com.books.application;

import com.books.domain.model.Book;
import com.books.domain.model.BookId;
import com.books.domain.port.AuthorRepository;
import com.books.domain.port.BookRepository;

public class GetBook {

	private final BookRepository books;
	private final AuthorRepository authors;

	public GetBook(BookRepository books, AuthorRepository authors) {
		this.books = books;
		this.authors = authors;
	}

	public BookView handle(BookId id) {
		Book book = Catalog.book(books, id);
		return new BookView(book, Catalog.author(authors, book.details().authorId()));
	}
}
