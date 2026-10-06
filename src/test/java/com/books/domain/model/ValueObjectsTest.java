package com.books.domain.model;

import java.util.Set;
import java.util.UUID;

import com.books.domain.exception.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ValueObjectsTest {

	@Test
	void identifiersParseUuidsAndRejectAnythingElse() {
		String id = "7c1f0e9a-3b5d-4f6a-8b7c-9d0e1f2a3b4c";

		assertThat(BookId.of(id).value()).isEqualTo(UUID.fromString(id));
		assertThat(AuthorId.of(" " + id + " ")).hasToString(id);
		assertThat(OwnerId.of(id)).isEqualTo(new OwnerId(UUID.fromString(id)));
		assertThat(BookId.newId()).isNotEqualTo(BookId.newId());
		assertThat(AuthorId.newId()).isNotEqualTo(AuthorId.newId());

		assertInvalid("id", () -> BookId.of("not-a-uuid"));
		assertInvalid("id", () -> BookId.of(" "));
		assertInvalid("authorId", () -> AuthorId.of(null));
		assertInvalid("ownerId", () -> new OwnerId(null));
	}

	@Test
	void titlePublisherAndGenreAreTrimmedRequiredAndBounded() {
		assertThat(new Title("  Dune ").value()).isEqualTo("Dune");
		assertThat(new Publisher(" Ace ")).hasToString("Ace");
		assertThat(new Genre(" Science Fiction ")).hasToString("Science Fiction");
		assertThat(new Title("x".repeat(Title.MAX_LENGTH)).value()).hasSize(Title.MAX_LENGTH);

		assertInvalid("title", () -> new Title(" "));
		assertInvalid("title", () -> new Title(null));
		assertInvalid("title", () -> new Title("x".repeat(Title.MAX_LENGTH + 1)));
		assertInvalid("publisher", () -> new Publisher(""));
		assertInvalid("publisher", () -> new Publisher("x".repeat(Publisher.MAX_LENGTH + 1)));
		assertInvalid("genre", () -> new Genre(null));
		assertInvalid("genre", () -> new Genre("x".repeat(Genre.MAX_LENGTH + 1)));
	}

	@Test
	void aPersonNameNeedsAFirstNameAndMayHaveNoLastName() {
		assertThat(new PersonName(" Frank ", " Herbert ").full()).isEqualTo("Frank Herbert");
		assertThat(new PersonName("Voltaire", " ").lastName()).isNull();
		assertThat(new PersonName("Voltaire", null).full()).isEqualTo("Voltaire");

		assertInvalid("firstName", () -> new PersonName(" ", "Herbert"));
		assertInvalid("lastName", () -> new PersonName("Frank", "x".repeat(PersonName.MAX_LENGTH + 1)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "9780441172719", "978-0-441-17271-9", "978 0 441 17271 9", "9791234567896" })
	void anIsbn13WithTheRightCheckDigitIsAcceptedAndNormalised(String input) {
		assertThat(new Isbn(input).value()).matches("97[89][0-9]{10}");
	}

	@ParameterizedTest
	@ValueSource(strings = { "9780441172710", "0441172717", "97804411727199", "9770441172719", "978044117271X", " " })
	void anythingThatIsNotAValidIsbn13IsRejected(String input) {
		assertInvalid("isbn", () -> new Isbn(input));
	}

	@Test
	void theIsbnCheckDigitFollowsTheStandard() {
		assertThat(Isbn.checkDigit("978044117271")).isEqualTo(9);
		assertThat(Isbn.checkDigit("978030640615")).isEqualTo(7);
		assertThat(new Isbn("9780441172719")).hasToString("9780441172719");
		assertInvalid("isbn", () -> new Isbn(null));
	}

	@Test
	void bookDetailsNeedEverythingButADescription() {
		var title = new Title("Dune");
		var isbn = new Isbn("9780441172719");
		var publisher = new Publisher("Ace");
		var author = AuthorId.newId();

		assertThat(new BookDetails(title, "  ", isbn, publisher, author, false).description()).isNull();
		assertThat(new BookDetails(title, " A desert planet. ", isbn, publisher, author, true).description())
				.isEqualTo("A desert planet.");

		assertInvalid("title", () -> new BookDetails(null, null, isbn, publisher, author, false));
		assertInvalid("isbn", () -> new BookDetails(title, null, null, publisher, author, false));
		assertInvalid("publisher", () -> new BookDetails(title, null, isbn, null, author, false));
		assertInvalid("authorId", () -> new BookDetails(title, null, isbn, publisher, null, false));
		assertInvalid("description", () -> new BookDetails(title, "x".repeat(BookDetails.MAX_DESCRIPTION_LENGTH + 1),
				isbn, publisher, author, false));
	}

	@Test
	void pagingAcceptsOnlyKnownSortFieldsAndSaneSizes() {
		Set<String> fields = Set.of("title", "createdAt");

		assertThat(Paging.of(0, 20, null, "title", fields)).isEqualTo(new Paging(0, 20, "title", true));
		assertThat(Paging.of(2, 5, "createdAt,desc", "title", fields)).isEqualTo(new Paging(2, 5, "createdAt", false));
		assertThat(Paging.of(0, 1, " title , ASC ", "title", fields).ascending()).isTrue();
		assertThat(Paging.of(0, Paging.MAX_SIZE, "title", "title", fields).size()).isEqualTo(Paging.MAX_SIZE);

		assertInvalid("sort", () -> Paging.of(0, 20, "isbn", "title", fields));
		assertInvalid("sort", () -> Paging.of(0, 20, "title,sideways", "title", fields));
		assertInvalid("sort", () -> Paging.of(0, 20, "title,asc,extra", "title", fields));
		assertInvalid("page", () -> Paging.of(-1, 20, null, "title", fields));
		assertInvalid("size", () -> Paging.of(0, 0, null, "title", fields));
		assertInvalid("size", () -> Paging.of(0, Paging.MAX_SIZE + 1, null, "title", fields));
	}

	@Test
	void searchesIgnoreBlankTextFilters() {
		var paging = new Paging(0, 20, "title", true);

		var books = new BookSearch(" dune ", null, "  ", null, null, paging);
		assertThat(books.title()).isEqualTo("dune");
		assertThat(books.publisher()).isNull();

		var authors = new AuthorSearch("", " fantasy ", paging);
		assertThat(authors.name()).isNull();
		assertThat(authors.genre()).isEqualTo("fantasy");
	}

	@Test
	void aPageOfResultsCanBeMappedAndIsImmutable() {
		var page = new PageResult<>(new java.util.ArrayList<>(java.util.List.of(1, 2)), 7, 0, 2);

		assertThat(page.map(String::valueOf)).isEqualTo(new PageResult<>(java.util.List.of("1", "2"), 7, 0, 2));
		assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> page.items().add(3));
	}

	@Test
	void aCatalogActorNeedsAUser() {
		var user = new OwnerId(UUID.randomUUID());

		assertThat(new CatalogActor(user, false).is(user)).isTrue();
		assertThat(new CatalogActor(user, false).is(new OwnerId(UUID.randomUUID()))).isFalse();
		assertInvalid("userId", () -> new CatalogActor(null, true));
	}

	static void assertInvalid(String field, org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
		assertThatExceptionOfType(InvalidValueException.class).isThrownBy(call)
				.satisfies(ex -> assertThat(ex.field()).isEqualTo(field))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("invalid-value"));
	}
}
