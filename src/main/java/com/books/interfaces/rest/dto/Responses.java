package com.books.interfaces.rest.dto;

import java.time.Instant;
import java.util.List;

import com.books.application.BookView;
import com.books.domain.model.Author;
import com.books.domain.model.Book;
import com.books.domain.model.OwnerId;
import com.books.domain.model.PageResult;

/** Response bodies. */
public final class Responses {

	private Responses() {
	}

	public record AuthorResponse(String id, String firstName, String lastName, String genre, Instant createdAt,
			Instant updatedAt) {

		public static AuthorResponse from(Author author) {
			return new AuthorResponse(author.id().toString(), author.name().firstName(), author.name().lastName(),
					author.genre().value(), author.createdAt(), author.updatedAt());
		}
	}

	/** The author as shown inside a book. */
	public record AuthorSummary(String id, String firstName, String lastName) {
	}

	/** {@code ownerId} is the platform user ID of the owner, or null for a book the catalog itself owns. */
	public record BookResponse(String id, String title, String description, String isbn, String publisher,
			AuthorSummary author, String ownerId, boolean completed, Instant createdAt, Instant updatedAt) {

		public static BookResponse from(BookView view) {
			Book book = view.book();
			Author author = view.author();
			return new BookResponse(book.id().toString(), book.details().title().value(), book.details().description(),
					book.details().isbn().value(), book.details().publisher().value(),
					new AuthorSummary(author.id().toString(), author.name().firstName(), author.name().lastName()),
					book.ownerId().map(OwnerId::toString).orElse(null), book.details().completed(), book.createdAt(),
					book.updatedAt());
		}
	}

	public record PageResponse<T>(List<T> items, long total, int page, int size) {

		public static <T> PageResponse<T> from(PageResult<T> result) {
			return new PageResponse<>(result.items(), result.total(), result.page(), result.size());
		}
	}
}
