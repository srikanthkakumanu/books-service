package com.books.interfaces.rest.dto;

import com.books.domain.model.AuthorId;
import com.books.domain.model.BookDetails;
import com.books.domain.model.Genre;
import com.books.domain.model.Isbn;
import com.books.domain.model.OwnerId;
import com.books.domain.model.PersonName;
import com.books.domain.model.Publisher;
import com.books.domain.model.Title;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request bodies. Bean Validation rejects what is missing; the value objects enforce the rest. */
public final class Requests {

	private Requests() {
	}

	/** {@code ownerId} is only for a catalog manager adding a book for another user. */
	public record CreateBook(@NotBlank @Size(max = Title.MAX_LENGTH) String title,
			@Size(max = BookDetails.MAX_DESCRIPTION_LENGTH) String description, @NotBlank String isbn,
			@NotBlank @Size(max = Publisher.MAX_LENGTH) String publisher, @NotBlank String authorId, Boolean completed,
			String ownerId) {

		public BookDetails details() {
			return Requests.details(title, description, isbn, publisher, authorId, completed);
		}

		public OwnerId owner() {
			return ownerId == null || ownerId.isBlank() ? null : OwnerId.of(ownerId);
		}
	}

	public record UpdateBook(@NotBlank @Size(max = Title.MAX_LENGTH) String title,
			@Size(max = BookDetails.MAX_DESCRIPTION_LENGTH) String description, @NotBlank String isbn,
			@NotBlank @Size(max = Publisher.MAX_LENGTH) String publisher, @NotBlank String authorId,
			Boolean completed) {

		public BookDetails details() {
			return Requests.details(title, description, isbn, publisher, authorId, completed);
		}
	}

	public record TransferBook(@NotBlank String ownerId) {
	}

	public record SaveAuthor(@NotBlank @Size(max = PersonName.MAX_LENGTH) String firstName,
			@Size(max = PersonName.MAX_LENGTH) String lastName, @NotBlank @Size(max = Genre.MAX_LENGTH) String genre) {

		public PersonName name() {
			return new PersonName(firstName, lastName);
		}

		public Genre genreValue() {
			return new Genre(genre);
		}
	}

	private static BookDetails details(String title, String description, String isbn, String publisher,
			String authorId, Boolean completed) {
		return new BookDetails(new Title(title), description, new Isbn(isbn), new Publisher(publisher),
				AuthorId.of(authorId), Boolean.TRUE.equals(completed));
	}
}
