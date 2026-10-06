package com.books.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.books.domain.exception.AuthorInUseException;
import com.books.domain.exception.DuplicateBookException;
import com.books.domain.model.Author;
import com.books.domain.model.AuthorId;
import com.books.domain.model.AuthorSearch;
import com.books.domain.model.Book;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/** The catalog repositories against real Postgres, with the schema created by Flyway and validated by Hibernate. */
@Testcontainers
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ JpaBookRepository.class, JpaAuthorRepository.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaCatalogRepositoriesIT {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

	private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant LATER = Instant.parse("2026-02-01T00:00:00Z");
	private static final String[] ISBNS = { "9780441172719", "9780141439587", "9780306406157", "9791234567896" };

	@Autowired
	private JpaBookRepository books;

	@Autowired
	private JpaAuthorRepository authors;

	@Autowired
	private JdbcTemplate jdbc;

	private final OwnerId alice = new OwnerId(UUID.randomUUID());
	private Author herbert;
	private Author austen;

	@BeforeEach
	void twoAuthors() {
		jdbc.update("delete from book");
		jdbc.update("delete from author");
		herbert = authors.save(Author.create(AuthorId.newId(), new PersonName("Frank", "Herbert"),
				new Genre("Science Fiction"), CREATED));
		austen = authors.save(Author.create(AuthorId.newId(), new PersonName("Jane", "Austen"), new Genre("Romance"),
				CREATED));
	}

	@Test
	void flywayCreatedTheSchema() {
		assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success", Integer.class))
				.isEqualTo(1);
		assertThat(jdbc.queryForList("select column_name from information_schema.columns where table_name = 'book'",
				String.class)).contains("owner_id", "author_id", "isbn").doesNotContain("user_name", "user_id");
	}

	@Test
	void aBookIsStoredAndReadBackWithItsOwner() {
		Book book = books.save(book("Dune", ISBNS[0], herbert, alice));

		Book found = books.findById(book.id()).orElseThrow();

		assertThat(found.details()).isEqualTo(book.details());
		assertThat(found.ownerId()).contains(alice);
		assertThat(found.createdAt()).isEqualTo(CREATED);
		assertThat(books.exists(book.id())).isTrue();
		assertThat(books.findByIsbn(new Isbn(ISBNS[0]))).map(Book::id).contains(book.id());
		assertThat(books.findByIsbn(new Isbn(ISBNS[1]))).isEmpty();
		assertThat(books.findById(BookId.newId())).isEmpty();
	}

	@Test
	void aCatalogOwnedBookHasANullOwnerColumn() {
		Book book = books.save(Book.catalogOwned(BookId.newId(), details("Emma", ISBNS[1], austen), CREATED));

		assertThat(books.findById(book.id()).orElseThrow().ownerId()).isEmpty();
		assertThat(jdbc.queryForObject("select count(*) from book where owner_id is null", Integer.class)).isEqualTo(1);
	}

	@Test
	void savingAgainUpdatesTheRowAndKeepsTheCreationTime() {
		Book book = books.save(book("Dune", ISBNS[0], herbert, alice));
		var manager = new CatalogActor(new OwnerId(UUID.randomUUID()), true);

		books.save(book.revise(details("Dune Messiah", ISBNS[0], herbert), manager, LATER));

		Book found = books.findById(book.id()).orElseThrow();
		assertThat(found.details().title().value()).isEqualTo("Dune Messiah");
		assertThat(found.createdAt()).isEqualTo(CREATED);
		assertThat(found.updatedAt()).isEqualTo(LATER);
		assertThat(jdbc.queryForObject("select count(*) from book", Integer.class)).isEqualTo(1);
		assertThat(jdbc.queryForObject("select version from book", Long.class)).isEqualTo(1);
	}

	@Test
	void theDatabaseRefusesASecondBookWithTheSameIsbn() {
		books.save(book("Dune", ISBNS[0], herbert, alice));

		assertThatExceptionOfType(DuplicateBookException.class)
				.isThrownBy(() -> books.save(book("Another Dune", ISBNS[0], herbert, alice)));
		assertThat(jdbc.queryForObject("select count(*) from book", Integer.class)).isEqualTo(1);
	}

	@Test
	void theDatabaseRefusesABookWithoutAnExistingAuthor() {
		assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(() -> jdbc.update(
				"insert into book (id, title, isbn, publisher, author_id, created_at, updated_at) values (?, 'Orphan', ?, 'Ace', ?, now(), now())",
				UUID.randomUUID(), ISBNS[2], UUID.randomUUID()));
	}

	@Test
	void booksAreFilteredSortedAndPaged() {
		OwnerId bob = new OwnerId(UUID.randomUUID());
		books.save(book("Dune", ISBNS[0], herbert, alice));
		books.save(book("Children of Dune", ISBNS[2], herbert, bob));
		books.save(book("Emma", ISBNS[1], austen, alice));
		books.save(Book.catalogOwned(BookId.newId(), details("100% Wool_Gathering", ISBNS[3], austen), CREATED));

		assertThat(titles(new BookSearch(null, null, null, null, null, paging(0, 10, "title", true))))
				.containsExactly("100% Wool_Gathering", "Children of Dune", "Dune", "Emma");
		assertThat(titles(new BookSearch("dUNe", null, null, null, null, paging(0, 10, "title", false))))
				.containsExactly("Dune", "Children of Dune");
		assertThat(titles(new BookSearch(null, null, null, austen.id(), null, paging(0, 10, "title", true))))
				.containsExactly("100% Wool_Gathering", "Emma");
		assertThat(titles(new BookSearch(null, null, null, null, alice, paging(0, 10, "title", true))))
				.containsExactly("Dune", "Emma");
		assertThat(titles(new BookSearch(null, new Isbn(ISBNS[1]), null, null, null, paging(0, 10, "title", true))))
				.containsExactly("Emma");
		assertThat(titles(new BookSearch(null, null, "ACE", null, null, paging(0, 10, "title", true)))).hasSize(4);
		// Wildcard characters in the caller's text are taken literally.
		assertThat(titles(new BookSearch("%", null, null, null, null, paging(0, 10, "title", true))))
				.containsExactly("100% Wool_Gathering");
		assertThat(titles(new BookSearch("l_g", null, null, null, null, paging(0, 10, "title", true))))
				.containsExactly("100% Wool_Gathering");

		var secondPage = books.search(new BookSearch(null, null, null, null, null, paging(1, 3, "title", true)));
		assertThat(secondPage.total()).isEqualTo(4);
		assertThat(secondPage.page()).isEqualTo(1);
		assertThat(secondPage.items()).extracting(book -> book.details().title().value()).containsExactly("Emma");
	}

	@Test
	void authorsAreStoredUpdatedSearchedAndFoundById() {
		authors.save(herbert.revise(new PersonName("Franklin", "Herbert"), new Genre("Fiction"), LATER));
		Author voltaire = authors.save(Author.create(AuthorId.newId(), new PersonName("Voltaire", null),
				new Genre("Philosophy"), CREATED));

		assertThat(authors.findById(herbert.id()).orElseThrow().name().firstName()).isEqualTo("Franklin");
		assertThat(authors.findById(herbert.id()).orElseThrow().createdAt()).isEqualTo(CREATED);
		assertThat(authors.findById(voltaire.id()).orElseThrow().name().lastName()).isNull();
		assertThat(authors.exists(austen.id())).isTrue();
		assertThat(authors.exists(AuthorId.newId())).isFalse();
		assertThat(authors.findByIds(List.of(herbert.id(), austen.id(), AuthorId.newId()))).hasSize(2);

		var byName = authors.search(new AuthorSearch("aust", null, paging(0, 10, "lastName", true)));
		assertThat(byName.items()).extracting(author -> author.name().lastName()).containsExactly("Austen");
		var byFirstName = authors.search(new AuthorSearch("frank", null, paging(0, 10, "lastName", true)));
		assertThat(byFirstName.total()).isEqualTo(1);
		var byGenre = authors.search(new AuthorSearch(null, "ROMAN", paging(0, 10, "lastName", true)));
		assertThat(byGenre.items()).extracting(Author::id).containsExactly(austen.id());
		var sorted = authors.search(new AuthorSearch(null, null, paging(0, 2, "genre", false)));
		assertThat(sorted.total()).isEqualTo(3);
		assertThat(sorted.items()).extracting(author -> author.genre().value()).containsExactly("Romance", "Philosophy");
	}

	@Test
	void anAuthorWithBooksCannotBeDeletedAndOneWithoutCan() {
		Book book = books.save(book("Dune", ISBNS[0], herbert, alice));

		assertThat(books.existsByAuthor(herbert.id())).isTrue();
		assertThat(books.existsByAuthor(austen.id())).isFalse();
		assertThatExceptionOfType(AuthorInUseException.class).isThrownBy(() -> authors.delete(herbert.id()));
		assertThat(authors.exists(herbert.id())).isTrue();

		books.delete(book.id());
		authors.delete(herbert.id());
		authors.delete(austen.id());

		assertThat(books.exists(book.id())).isFalse();
		assertThat(jdbc.queryForObject("select count(*) from author", Integer.class)).isZero();
	}

	private List<String> titles(BookSearch search) {
		return books.search(search).items().stream().map(book -> book.details().title().value()).toList();
	}

	private static Paging paging(int page, int size, String sortBy, boolean ascending) {
		return new Paging(page, size, sortBy, ascending);
	}

	private Book book(String title, String isbn, Author author, OwnerId owner) {
		return Book.create(BookId.newId(), details(title, isbn, author), null, new CatalogActor(owner, false), CREATED);
	}

	private static BookDetails details(String title, String isbn, Author author) {
		return new BookDetails(new Title(title), null, new Isbn(isbn), new Publisher("Ace Books"), author.id(), false);
	}
}
