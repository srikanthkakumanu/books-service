package com.books.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.books.application.SeedCatalog.SeedAuthor;
import com.books.application.SeedCatalog.SeedBook;
import com.books.domain.exception.AuthorInUseException;
import com.books.domain.exception.AuthorNotFoundException;
import com.books.domain.exception.BookNotFoundException;
import com.books.domain.exception.DuplicateBookException;
import com.books.domain.exception.InvalidValueException;
import com.books.domain.exception.OperationNotPermittedException;
import com.books.domain.exception.OwnerNotEligibleException;
import com.books.domain.model.Author;
import com.books.domain.model.AuthorId;
import com.books.domain.model.AuthorSearch;
import com.books.domain.model.BookDetails;
import com.books.domain.model.BookId;
import com.books.domain.model.BookSearch;
import com.books.domain.model.CatalogActor;
import com.books.domain.model.Genre;
import com.books.domain.model.Isbn;
import com.books.domain.model.OwnerId;
import com.books.domain.model.Paging;
import com.books.domain.model.PersonName;
import com.books.domain.model.Publisher;
import com.books.domain.model.Title;
import com.books.domain.model.UserStanding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/** The catalog's use cases against in-memory ports. */
class CatalogUseCasesTest {

	private static final Instant NOW = Instant.parse("2026-03-01T10:00:00Z");
	private static final String DUNE = "9780441172719";
	private static final String EMMA = "9780141439587";
	private static final Paging FIRST_PAGE = new Paging(0, 20, "title", true);

	private final InMemoryCatalog.Books books = new InMemoryCatalog.Books();
	private final InMemoryCatalog.Authors authors = new InMemoryCatalog.Authors();
	private final InMemoryCatalog.Directory directory = new InMemoryCatalog.Directory();
	private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

	private final CreateBook createBook = new CreateBook(books, authors, directory, clock);
	private final UpdateBook updateBook = new UpdateBook(books, authors, clock);
	private final DeleteBook deleteBook = new DeleteBook(books);
	private final TransferBook transferBook = new TransferBook(books, authors, directory, clock);
	private final GetBook getBook = new GetBook(books, authors);
	private final SearchBooks searchBooks = new SearchBooks(books, authors);
	private final CreateAuthor createAuthor = new CreateAuthor(authors, clock);
	private final UpdateAuthor updateAuthor = new UpdateAuthor(authors, clock);
	private final DeleteAuthor deleteAuthor = new DeleteAuthor(authors, books);
	private final GetAuthor getAuthor = new GetAuthor(authors);
	private final SearchAuthors searchAuthors = new SearchAuthors(authors);
	private final SeedCatalog seedCatalog = new SeedCatalog(authors, books, clock);

	private final OwnerId alice = new OwnerId(UUID.randomUUID());
	private final OwnerId bob = new OwnerId(UUID.randomUUID());
	private final CatalogActor aliceActor = new CatalogActor(alice, false);
	private final CatalogActor bobActor = new CatalogActor(bob, false);
	private final CatalogActor manager = new CatalogActor(new OwnerId(UUID.randomUUID()), true);
	private Author herbert;

	@BeforeEach
	void anAuthorExists() {
		herbert = createAuthor.handle(new PersonName("Frank", "Herbert"), new Genre("Science Fiction"));
		directory.users.put(alice, UserStanding.ACTIVE);
		directory.users.put(bob, UserStanding.ACTIVE);
	}

	// --- books

	@Test
	void addingABookForYourselfStoresItWithItsAuthorAndAsksNobodyAboutUsers() {
		BookView view = createBook.handle(new CreateBook.Command(details("Dune", DUNE), null), aliceActor);

		assertThat(view.book().ownerId()).contains(alice);
		assertThat(view.book().createdAt()).isEqualTo(NOW);
		assertThat(view.author()).isSameAs(herbert);
		assertThat(books.stored).containsKey(view.book().id());
		assertThat(directory.lookups).isZero();
	}

	@Test
	void aManagerAddsABookForAnActiveUserAfterAskingThePlatform() {
		BookView view = createBook.handle(new CreateBook.Command(details("Dune", DUNE), bob), manager);

		assertThat(view.book().ownerId()).contains(bob);
		assertThat(directory.lookups).isEqualTo(1);
	}

	@Test
	void aBookIsNotAddedForAnUnknownOrInactiveUser() {
		OwnerId stranger = new OwnerId(UUID.randomUUID());
		directory.users.put(bob, UserStanding.INACTIVE);

		assertThatExceptionOfType(OwnerNotEligibleException.class)
				.isThrownBy(() -> createBook.handle(new CreateBook.Command(details("Dune", DUNE), stranger), manager))
				.withMessageContaining("No user").satisfies(ex -> assertThat(ex.code()).isEqualTo("owner-not-eligible"));
		assertThatExceptionOfType(OwnerNotEligibleException.class)
				.isThrownBy(() -> createBook.handle(new CreateBook.Command(details("Dune", DUNE), bob), manager))
				.withMessageContaining("not active");
		assertThat(books.stored).isEmpty();
	}

	@Test
	void anOrdinaryUserCannotAddABookForSomeoneElse() {
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> createBook.handle(new CreateBook.Command(details("Dune", DUNE), bob), aliceActor));
		assertThat(books.stored).isEmpty();
		assertThat(directory.lookups).isZero();
	}

	@Test
	void aBookMustNameAnExistingAuthorAndAnUnusedIsbn() {
		createBook.handle(new CreateBook.Command(details("Dune", DUNE), null), aliceActor);
		var unknownAuthor = new BookDetails(new Title("Ghost"), null, new Isbn(EMMA), new Publisher("Ace"),
				AuthorId.newId(), false);

		assertThatExceptionOfType(InvalidValueException.class)
				.isThrownBy(() -> createBook.handle(new CreateBook.Command(unknownAuthor, null), aliceActor))
				.satisfies(ex -> assertThat(ex.field()).isEqualTo("authorId"));
		assertThatExceptionOfType(DuplicateBookException.class)
				.isThrownBy(() -> createBook.handle(new CreateBook.Command(details("Dune again", DUNE), null), bobActor))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("duplicate-book"));
		assertThat(books.stored).hasSize(1);
	}

	@Test
	void theOwnerUpdatesTheirBookAndMayKeepItsOwnIsbn() {
		BookId id = createBook.handle(new CreateBook.Command(details("Dune", DUNE), null), aliceActor).book().id();

		BookView updated = updateBook.handle(id, details("Dune (revised)", DUNE), aliceActor);

		assertThat(updated.book().details().title().value()).isEqualTo("Dune (revised)");
		assertThat(updated.author()).isSameAs(herbert);
		assertThat(getBook.handle(id).book().details().title().value()).isEqualTo("Dune (revised)");
	}

	@Test
	void updatingIsRefusedForOthersForATakenIsbnAndForAMissingBook() {
		BookId dune = createBook.handle(new CreateBook.Command(details("Dune", DUNE), null), aliceActor).book().id();
		createBook.handle(new CreateBook.Command(details("Emma", EMMA), null), bobActor);

		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> updateBook.handle(dune, details("Hijacked", DUNE), bobActor));
		assertThatExceptionOfType(DuplicateBookException.class)
				.isThrownBy(() -> updateBook.handle(dune, details("Dune", EMMA), aliceActor));
		assertThatExceptionOfType(BookNotFoundException.class)
				.isThrownBy(() -> updateBook.handle(BookId.newId(), details("Dune", DUNE), aliceActor))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("book-not-found"));
		assertThat(updateBook.handle(dune, details("Corrected", DUNE), manager).book().details().title().value())
				.isEqualTo("Corrected");
	}

	@Test
	void onlyTheOwnerOrAManagerDeletesABook() {
		BookId first = createBook.handle(new CreateBook.Command(details("Dune", DUNE), null), aliceActor).book().id();
		BookId second = createBook.handle(new CreateBook.Command(details("Emma", EMMA), null), aliceActor).book().id();

		assertThatExceptionOfType(OperationNotPermittedException.class).isThrownBy(() -> deleteBook.handle(first, bobActor));
		assertThat(books.stored).hasSize(2);

		deleteBook.handle(first, aliceActor);
		deleteBook.handle(second, manager);

		assertThat(books.stored).isEmpty();
		assertThatExceptionOfType(BookNotFoundException.class).isThrownBy(() -> deleteBook.handle(first, aliceActor));
		assertThatExceptionOfType(BookNotFoundException.class).isThrownBy(() -> getBook.handle(first));
	}

	@Test
	void aManagerTransfersABookToAnActiveUserOnly() {
		BookId id = createBook.handle(new CreateBook.Command(details("Dune", DUNE), null), aliceActor).book().id();
		OwnerId stranger = new OwnerId(UUID.randomUUID());

		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> transferBook.handle(id, bob, aliceActor));
		assertThatExceptionOfType(OwnerNotEligibleException.class)
				.isThrownBy(() -> transferBook.handle(id, stranger, manager));
		assertThat(getBook.handle(id).book().ownerId()).contains(alice);

		BookView transferred = transferBook.handle(id, bob, manager);

		assertThat(transferred.book().ownerId()).contains(bob);
		assertThat(transferred.author()).isSameAs(herbert);
		assertThatExceptionOfType(BookNotFoundException.class)
				.isThrownBy(() -> transferBook.handle(BookId.newId(), bob, manager));
	}

	@Test
	void searchingReturnsBooksWithTheirAuthorsAndCanBeLimitedToAnOwner() {
		Author austen = createAuthor.handle(new PersonName("Jane", "Austen"), new Genre("Romance"));
		createBook.handle(new CreateBook.Command(details("Dune", DUNE), null), aliceActor);
		createBook.handle(new CreateBook.Command(new BookDetails(new Title("Emma"), null, new Isbn(EMMA),
				new Publisher("Penguin"), austen.id(), false), null), bobActor);

		var all = searchBooks.handle(new BookSearch(null, null, null, null, null, FIRST_PAGE));
		var bobs = searchBooks.handle(new BookSearch(null, null, null, null, bob, FIRST_PAGE));

		assertThat(all.total()).isEqualTo(2);
		assertThat(all.items()).extracting(view -> view.author().name().lastName()).containsExactly("Herbert", "Austen");
		assertThat(bobs.items()).extracting(view -> view.book().details().title().value()).containsExactly("Emma");
	}

	// --- authors

	@Test
	void authorsAreCreatedReadUpdatedAndSearched() {
		Author updated = updateAuthor.handle(herbert.id(), new PersonName("Franklin", "Herbert"), new Genre("Fiction"));

		assertThat(updated.name().firstName()).isEqualTo("Franklin");
		assertThat(updated.createdAt()).isEqualTo(NOW);
		assertThat(getAuthor.handle(herbert.id()).genre().value()).isEqualTo("Fiction");
		assertThat(searchAuthors.handle(new AuthorSearch(null, null, FIRST_PAGE)).total()).isEqualTo(1);
		assertThatExceptionOfType(AuthorNotFoundException.class).isThrownBy(() -> getAuthor.handle(AuthorId.newId()))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("author-not-found"));
		assertThatExceptionOfType(AuthorNotFoundException.class)
				.isThrownBy(() -> updateAuthor.handle(AuthorId.newId(), herbert.name(), herbert.genre()));
	}

	@Test
	void anAuthorWithBooksCannotBeDeletedUntilTheBooksAreGone() {
		BookId id = createBook.handle(new CreateBook.Command(details("Dune", DUNE), null), aliceActor).book().id();

		assertThatExceptionOfType(AuthorInUseException.class).isThrownBy(() -> deleteAuthor.handle(herbert.id()))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("author-in-use"));
		assertThat(authors.stored).containsKey(herbert.id());

		deleteBook.handle(id, aliceActor);
		deleteAuthor.handle(herbert.id());

		assertThat(authors.stored).isEmpty();
		assertThatExceptionOfType(AuthorNotFoundException.class).isThrownBy(() -> deleteAuthor.handle(herbert.id()));
	}

	// --- seed

	@Test
	void seedingAddsCatalogOwnedBooksAndIsSafeToRepeat() {
		String authorId = UUID.randomUUID().toString();
		var seedAuthors = List.of(new SeedAuthor(authorId, "Jane", "Austen", "Romance"));
		var seedBooks = List.of(new SeedBook(UUID.randomUUID().toString(), "Emma", "A comedy of manners.", EMMA,
				"Penguin", authorId));

		assertThat(seedCatalog.handle(seedAuthors, seedBooks)).isEqualTo(new SeedCatalog.Result(1, 1));

		var emma = books.findByIsbn(new Isbn(EMMA)).orElseThrow();
		assertThat(emma.ownerId()).isEmpty();
		assertThat(emma.details().description()).isEqualTo("A comedy of manners.");
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> updateBook.handle(emma.id(), details("Mine now", EMMA), aliceActor));

		// A later edit survives a second load, and nothing is added twice.
		updateBook.handle(emma.id(), details("Emma (annotated)", EMMA), manager);
		assertThat(seedCatalog.handle(seedAuthors, seedBooks)).isEqualTo(new SeedCatalog.Result(0, 0));
		assertThat(books.stored).hasSize(1);
		assertThat(books.findById(emma.id()).orElseThrow().details().title().value()).isEqualTo("Emma (annotated)");
	}

	@Test
	void seedDataThatBreaksADomainRuleIsRejected() {
		String authorId = UUID.randomUUID().toString();
		var seedAuthors = List.of(new SeedAuthor(authorId, "Jane", "Austen", "Romance"));

		assertThatExceptionOfType(InvalidValueException.class).isThrownBy(() -> seedCatalog.handle(seedAuthors,
				List.of(new SeedBook(UUID.randomUUID().toString(), "Emma", null, "9780141439580", "Penguin", authorId))))
				.satisfies(ex -> assertThat(ex.field()).isEqualTo("isbn"));
		assertThatExceptionOfType(InvalidValueException.class).isThrownBy(() -> seedCatalog.handle(seedAuthors,
				List.of(new SeedBook(UUID.randomUUID().toString(), "Emma", null, EMMA, "Penguin",
						UUID.randomUUID().toString()))))
				.satisfies(ex -> assertThat(ex.field()).isEqualTo("authorId"));
		assertThatExceptionOfType(InvalidValueException.class)
				.isThrownBy(() -> seedCatalog.handle(
						List.of(new SeedAuthor(UUID.randomUUID().toString(), " ", "Austen", "Romance")), List.of()))
				.satisfies(ex -> assertThat(ex.field()).isEqualTo("firstName"));
	}

	private BookDetails details(String title, String isbn) {
		return new BookDetails(new Title(title), null, new Isbn(isbn), new Publisher("Ace"), herbert.id(), false);
	}
}
