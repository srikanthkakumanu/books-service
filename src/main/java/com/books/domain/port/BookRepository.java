package com.books.domain.port;

import java.util.Optional;

import com.books.domain.model.AuthorId;
import com.books.domain.model.Book;
import com.books.domain.model.BookId;
import com.books.domain.model.BookSearch;
import com.books.domain.model.Isbn;
import com.books.domain.model.PageResult;

public interface BookRepository {

	/** @throws com.books.domain.exception.DuplicateBookException if another book has the same ISBN */
	Book save(Book book);

	Optional<Book> findById(BookId id);

	Optional<Book> findByIsbn(Isbn isbn);

	boolean exists(BookId id);

	boolean existsByAuthor(AuthorId authorId);

	PageResult<Book> search(BookSearch search);

	void delete(BookId id);
}
