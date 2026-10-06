package com.books.application;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.books.domain.exception.AuthorNotFoundException;
import com.books.domain.exception.BookNotFoundException;
import com.books.domain.exception.DuplicateBookException;
import com.books.domain.exception.InvalidValueException;
import com.books.domain.exception.OwnerNotEligibleException;
import com.books.domain.model.Author;
import com.books.domain.model.AuthorId;
import com.books.domain.model.Book;
import com.books.domain.model.BookDetails;
import com.books.domain.model.BookId;
import com.books.domain.model.OwnerId;
import com.books.domain.model.UserStanding;
import com.books.domain.port.AuthorRepository;
import com.books.domain.port.BookRepository;
import com.books.domain.port.UserDirectoryPort;

/** Lookups and checks several use cases share. */
final class Catalog {

	private Catalog() {
	}

	static Book book(BookRepository books, BookId id) {
		return books.findById(id).orElseThrow(() -> new BookNotFoundException("No book with ID " + id));
	}

	static Author author(AuthorRepository authors, AuthorId id) {
		return authors.findById(id).orElseThrow(() -> new AuthorNotFoundException("No author with ID " + id));
	}

	/** The author a book names must exist; naming one that does not is a mistake in the request. */
	static Author authorOf(AuthorRepository authors, BookDetails details) {
		return authors.findById(details.authorId())
				.orElseThrow(() -> new InvalidValueException("authorId", "No author with ID " + details.authorId()));
	}

	static void requireIsbnFree(BookRepository books, BookDetails details, BookId self) {
		books.findByIsbn(details.isbn()).filter(other -> !other.id().equals(self)).ifPresent(other -> {
			throw new DuplicateBookException("Another book already has ISBN " + details.isbn());
		});
	}

	/** A book can only be given to a user the platform knows and who is active. */
	static void requireEligibleOwner(UserDirectoryPort directory, OwnerId owner) {
		UserStanding standing = directory.standingOf(owner);
		if (standing != UserStanding.ACTIVE) {
			throw new OwnerNotEligibleException(standing == UserStanding.UNKNOWN
					? "No user with ID " + owner
					: "User " + owner + " is not active");
		}
	}

	static List<BookView> withAuthors(AuthorRepository authors, List<Book> books) {
		Map<AuthorId, Author> byId = authors
				.findByIds(books.stream().map(book -> book.details().authorId()).collect(Collectors.toSet())).stream()
				.collect(Collectors.toMap(Author::id, Function.identity()));
		return books.stream().map(book -> new BookView(book, byId.get(book.details().authorId()))).toList();
	}
}
