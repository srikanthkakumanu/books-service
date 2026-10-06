package com.books.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.books.domain.exception.OperationNotPermittedException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

/** Who may add, change and hand over a book. */
class BookTest {

	private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant LATER = Instant.parse("2026-02-01T00:00:00Z");

	private final OwnerId alice = new OwnerId(UUID.randomUUID());
	private final OwnerId bob = new OwnerId(UUID.randomUUID());
	private final CatalogActor aliceActor = new CatalogActor(alice, false);
	private final CatalogActor bobActor = new CatalogActor(bob, false);
	private final CatalogActor manager = new CatalogActor(new OwnerId(UUID.randomUUID()), true);
	private final BookDetails details = details("Dune");

	@Test
	void aBookBelongsToWhoeverAddsIt() {
		Book book = Book.create(BookId.newId(), details, null, aliceActor, CREATED);

		assertThat(book.ownerId()).contains(alice);
		assertThat(book.isOwnedBy(alice)).isTrue();
		assertThat(book.isOwnedBy(bob)).isFalse();
		assertThat(book.details()).isEqualTo(details);
		assertThat(book.createdAt()).isEqualTo(CREATED);
		assertThat(book.updatedAt()).isEqualTo(CREATED);
	}

	@Test
	void namingYourselfAsOwnerIsTheSameAsNamingNobody() {
		assertThat(Book.create(BookId.newId(), details, alice, aliceActor, CREATED).ownerId()).contains(alice);
	}

	@Test
	void onlyAManagerMayAddABookForSomeoneElse() {
		assertThat(Book.create(BookId.newId(), details, bob, manager, CREATED).ownerId()).contains(bob);

		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> Book.create(BookId.newId(), details, bob, aliceActor, CREATED))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("operation-not-permitted"));
	}

	@Test
	void theOwnerChangesTheirBookAndTheCreationTimeStays() {
		Book book = Book.create(BookId.newId(), details, null, aliceActor, CREATED);

		Book revised = book.revise(details("Dune Messiah"), aliceActor, LATER);

		assertThat(revised.id()).isEqualTo(book.id());
		assertThat(revised.details().title().value()).isEqualTo("Dune Messiah");
		assertThat(revised.ownerId()).contains(alice);
		assertThat(revised.createdAt()).isEqualTo(CREATED);
		assertThat(revised.updatedAt()).isEqualTo(LATER);
		assertThat(book.details().title().value()).isEqualTo("Dune");
	}

	@Test
	void someoneElseCannotChangeABookButAManagerCan() {
		Book book = Book.create(BookId.newId(), details, null, aliceActor, CREATED);

		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> book.revise(details("Hijacked"), bobActor, LATER));
		assertThatExceptionOfType(OperationNotPermittedException.class).isThrownBy(() -> book.requireWriteAccess(bobActor));
		assertThatNoException().isThrownBy(() -> book.requireWriteAccess(aliceActor));
		assertThat(book.revise(details("Corrected"), manager, LATER).details().title().value()).isEqualTo("Corrected");
	}

	@Test
	void aCatalogOwnedBookHasNoOwnerAndIsChangedOnlyByAManager() {
		Book book = Book.catalogOwned(BookId.newId(), details, CREATED);

		assertThat(book.ownerId()).isEmpty();
		assertThat(book.isOwnedBy(alice)).isFalse();
		assertThatExceptionOfType(OperationNotPermittedException.class).isThrownBy(() -> book.requireWriteAccess(aliceActor));
		assertThatNoException().isThrownBy(() -> book.requireWriteAccess(manager));
	}

	@Test
	void onlyAManagerTransfersABookEvenTheOwnerCannot() {
		Book book = Book.create(BookId.newId(), details, null, aliceActor, CREATED);

		Book transferred = book.transferTo(bob, manager, LATER);

		assertThat(transferred.ownerId()).contains(bob);
		assertThat(transferred.updatedAt()).isEqualTo(LATER);
		assertThat(transferred.details()).isEqualTo(details);
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> book.transferTo(bob, aliceActor, LATER));
		assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> book.transferTo(null, manager, LATER));
	}

	@Test
	void rehydratingKeepsEverythingAsStored() {
		BookId id = BookId.newId();

		Book book = Book.rehydrate(id, details, bob, CREATED, LATER);

		assertThat(book.id()).isEqualTo(id);
		assertThat(book.ownerId()).contains(bob);
		assertThat(book.createdAt()).isEqualTo(CREATED);
		assertThat(book.updatedAt()).isEqualTo(LATER);
		assertThat(Book.rehydrate(id, details, null, CREATED, LATER).ownerId()).isEmpty();
	}

	@Test
	void anAuthorIsRevisedWithoutLosingIdentityOrCreationTime() {
		Author author = Author.create(AuthorId.newId(), new PersonName("Frank", "Herbert"), new Genre("Science Fiction"),
				CREATED);

		Author revised = author.revise(new PersonName("Franklin", "Herbert"), new Genre("Fiction"), LATER);

		assertThat(revised.id()).isEqualTo(author.id());
		assertThat(revised.name().firstName()).isEqualTo("Franklin");
		assertThat(revised.genre().value()).isEqualTo("Fiction");
		assertThat(revised.createdAt()).isEqualTo(CREATED);
		assertThat(revised.updatedAt()).isEqualTo(LATER);
		assertThat(author.name().firstName()).isEqualTo("Frank");
		assertThat(Author.rehydrate(author.id(), author.name(), author.genre(), CREATED, LATER).updatedAt())
				.isEqualTo(LATER);
	}

	private static BookDetails details(String title) {
		return new BookDetails(new Title(title), null, new Isbn("9780441172719"), new Publisher("Ace"),
				new AuthorId(UUID.fromString("11111111-2222-3333-4444-555555555555")), false);
	}
}
