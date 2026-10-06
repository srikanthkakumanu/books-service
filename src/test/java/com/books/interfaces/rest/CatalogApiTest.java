package com.books.interfaces.rest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.books.application.BookView;
import com.books.application.CreateAuthor;
import com.books.application.CreateBook;
import com.books.application.DeleteAuthor;
import com.books.application.DeleteBook;
import com.books.application.GetAuthor;
import com.books.application.GetBook;
import com.books.application.SearchAuthors;
import com.books.application.SearchBooks;
import com.books.application.TransferBook;
import com.books.application.UpdateAuthor;
import com.books.application.UpdateBook;
import com.books.domain.exception.AuthorInUseException;
import com.books.domain.exception.AuthorNotFoundException;
import com.books.domain.exception.BookNotFoundException;
import com.books.domain.exception.DuplicateBookException;
import com.books.domain.exception.OperationNotPermittedException;
import com.books.domain.exception.OwnerNotEligibleException;
import com.books.domain.exception.UserDirectoryUnavailableException;
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
import com.books.domain.model.PageResult;
import com.books.domain.model.Paging;
import com.books.domain.model.PersonName;
import com.books.domain.model.Publisher;
import com.books.domain.model.Title;
import com.books.interfaces.rest.error.ProblemResponses;
import com.books.interfaces.security.SecurityConfiguration;
import com.platform.security.autoconfigure.PlatformSecurityAutoConfiguration;
import com.platform.security.jwt.PlatformAuthoritiesConverter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Validation, error mapping and authorization rules of the catalog API, with use cases mocked. */
@WebMvcTest(properties = {
		"platform.security.jwt.issuer-uri=http://localhost:8080/realms/platform",
		"platform.security.jwt.jwk-set-uri=http://localhost:1/certs",
		"platform.security.jwt.audience=books-service" })
@ImportAutoConfiguration(PlatformSecurityAutoConfiguration.class)
@Import({ SecurityConfiguration.class, ProblemResponses.class })
class CatalogApiTest {

	private static final String ALICE = "7c1f0e9a-3b5d-4f6a-8b7c-9d0e1f2a3b4c";
	private static final String BOB = "11111111-2222-3333-4444-555555555555";
	private static final String BOOK = "aaaaaaaa-0000-4000-8000-000000000001";
	private static final String AUTHOR = "bbbbbbbb-0000-4000-8000-000000000001";
	private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");
	private static final String VALID_BOOK = """
			{"title": "Dune", "description": "A desert planet.", "isbn": "978-0-441-17271-9", "publisher": "Ace",
			 "authorId": "%s"}""".formatted(AUTHOR);
	private static final String VALID_AUTHOR = """
			{"firstName": "Frank", "lastName": "Herbert", "genre": "Science Fiction"}""";

	@Autowired
	private MockMvc mvc;

	@MockitoBean private CreateBook createBook;
	@MockitoBean private UpdateBook updateBook;
	@MockitoBean private DeleteBook deleteBook;
	@MockitoBean private TransferBook transferBook;
	@MockitoBean private GetBook getBook;
	@MockitoBean private SearchBooks searchBooks;
	@MockitoBean private CreateAuthor createAuthor;
	@MockitoBean private UpdateAuthor updateAuthor;
	@MockitoBean private DeleteAuthor deleteAuthor;
	@MockitoBean private GetAuthor getAuthor;
	@MockitoBean private SearchAuthors searchAuthors;

	// --- login is required for everything

	@Test
	void withoutATokenEveryCatalogEndpointIsUnauthorized() throws Exception {
		mvc.perform(get("/api/v1/books")).andExpect(status().isUnauthorized()).andExpect(problem("unauthorized"));
		mvc.perform(get("/api/v1/books/" + BOOK)).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content(VALID_BOOK))
				.andExpect(status().isUnauthorized());
		mvc.perform(put("/api/v1/books/" + BOOK).contentType(MediaType.APPLICATION_JSON).content(VALID_BOOK))
				.andExpect(status().isUnauthorized());
		mvc.perform(delete("/api/v1/books/" + BOOK)).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/v1/authors")).andExpect(status().isUnauthorized()).andExpect(problem("unauthorized"));
		mvc.perform(post("/api/v1/authors").contentType(MediaType.APPLICATION_JSON).content(VALID_AUTHOR))
				.andExpect(status().isUnauthorized());
		mvc.perform(get("/api/books/ping")).andExpect(status().isUnauthorized());
		verifyNoInteractions(searchBooks, getBook, createBook, updateBook, deleteBook, searchAuthors, createAuthor);
	}

	// --- being logged in is not enough: a catalog permission is needed

	@Test
	void aLoggedInUserWithoutACatalogRoleCanDoNothing() throws Exception {
		var plainUser = token(ALICE);

		mvc.perform(get("/api/v1/books").with(plainUser)).andExpect(status().isForbidden()).andExpect(problem("forbidden"));
		mvc.perform(get("/api/v1/books/" + BOOK).with(plainUser)).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/authors").with(plainUser)).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/authors/" + AUTHOR).with(plainUser)).andExpect(status().isForbidden());
		mvc.perform(post("/api/v1/books").with(plainUser).contentType(MediaType.APPLICATION_JSON).content(VALID_BOOK))
				.andExpect(status().isForbidden());
		verifyNoInteractions(searchBooks, getBook, searchAuthors, getAuthor, createBook);
	}

	@Test
	void aReaderReadsButCannotWrite() throws Exception {
		var reader = token(ALICE, "books:read");
		given(getBook.handle(BookId.of(BOOK))).willReturn(view(ALICE));
		given(getAuthor.handle(AuthorId.of(AUTHOR))).willReturn(author());

		mvc.perform(get("/api/v1/books/" + BOOK).with(reader)).andExpect(status().isOk());
		mvc.perform(get("/api/v1/authors/" + AUTHOR).with(reader)).andExpect(status().isOk());

		mvc.perform(post("/api/v1/books").with(reader).contentType(MediaType.APPLICATION_JSON).content(VALID_BOOK))
				.andExpect(status().isForbidden()).andExpect(problem("forbidden"));
		mvc.perform(put("/api/v1/books/" + BOOK).with(reader).contentType(MediaType.APPLICATION_JSON).content(VALID_BOOK))
				.andExpect(status().isForbidden());
		mvc.perform(delete("/api/v1/books/" + BOOK).with(reader)).andExpect(status().isForbidden());
		mvc.perform(put("/api/v1/books/" + BOOK + "/owner").with(reader).contentType(MediaType.APPLICATION_JSON)
				.content("{\"ownerId\": \"" + BOB + "\"}")).andExpect(status().isForbidden());
		verifyNoInteractions(createBook, updateBook, deleteBook, transferBook);
	}

	@Test
	void anEditorCannotTransferBooksOrChangeAuthors() throws Exception {
		var editor = token(ALICE, "books:read", "books:write");

		mvc.perform(put("/api/v1/books/" + BOOK + "/owner").with(editor).contentType(MediaType.APPLICATION_JSON)
				.content("{\"ownerId\": \"" + BOB + "\"}")).andExpect(status().isForbidden());
		mvc.perform(post("/api/v1/authors").with(editor).contentType(MediaType.APPLICATION_JSON).content(VALID_AUTHOR))
				.andExpect(status().isForbidden());
		mvc.perform(put("/api/v1/authors/" + AUTHOR).with(editor).contentType(MediaType.APPLICATION_JSON)
				.content(VALID_AUTHOR)).andExpect(status().isForbidden());
		mvc.perform(delete("/api/v1/authors/" + AUTHOR).with(editor)).andExpect(status().isForbidden());
		verifyNoInteractions(transferBook, createAuthor, updateAuthor, deleteAuthor);
	}

	@Test
	void aRoleNameAloneGrantsNothingOnlyPermissionsDo() throws Exception {
		JwtRequestPostProcessor roleOnly = jwt().jwt(token -> token.subject(ALICE)
				.claim("realm_access", Map.of("roles", List.of("CATALOG_MANAGER", "ADMIN", "MANAGER"))))
				.authorities(new PlatformAuthoritiesConverter());

		mvc.perform(get("/api/v1/books").with(roleOnly)).andExpect(status().isForbidden());
		mvc.perform(delete("/api/v1/books/" + BOOK).with(roleOnly)).andExpect(status().isForbidden());
	}

	// --- books

	@Test
	void anEditorAddsABookForThemselves() throws Exception {
		given(createBook.handle(any(), any())).willReturn(view(ALICE));

		mvc.perform(post("/api/v1/books").with(token(ALICE, "books:write")).contentType(MediaType.APPLICATION_JSON)
				.content(VALID_BOOK))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/books/" + BOOK))
				.andExpect(jsonPath("$.id").value(BOOK))
				.andExpect(jsonPath("$.title").value("Dune"))
				.andExpect(jsonPath("$.isbn").value("9780441172719"))
				.andExpect(jsonPath("$.author.id").value(AUTHOR))
				.andExpect(jsonPath("$.author.lastName").value("Herbert"))
				.andExpect(jsonPath("$.ownerId").value(ALICE))
				.andExpect(jsonPath("$.userName").doesNotExist());

		var command = ArgumentCaptor.forClass(CreateBook.Command.class);
		var actor = ArgumentCaptor.forClass(CatalogActor.class);
		verify(createBook).handle(command.capture(), actor.capture());
		assertThat(command.getValue().ownerId()).isNull();
		assertThat(command.getValue().details().isbn()).isEqualTo(new Isbn("9780441172719"));
		assertThat(command.getValue().details().description()).isEqualTo("A desert planet.");
		assertThat(actor.getValue()).isEqualTo(new CatalogActor(OwnerId.of(ALICE), false));
	}

	@Test
	void theCallerIsTakenFromTheTokenAndManagementFromItsPermissions() throws Exception {
		given(createBook.handle(any(), any())).willReturn(view(BOB));
		String forBob = VALID_BOOK.replace("}", ", \"ownerId\": \"" + BOB + "\", \"completed\": true}");

		mvc.perform(post("/api/v1/books").with(token(ALICE, "books:write", "books:manage"))
				.contentType(MediaType.APPLICATION_JSON).content(forBob)).andExpect(status().isCreated());

		var command = ArgumentCaptor.forClass(CreateBook.Command.class);
		var actor = ArgumentCaptor.forClass(CatalogActor.class);
		verify(createBook).handle(command.capture(), actor.capture());
		assertThat(command.getValue().ownerId()).isEqualTo(OwnerId.of(BOB));
		assertThat(command.getValue().details().completed()).isTrue();
		assertThat(actor.getValue()).isEqualTo(new CatalogActor(OwnerId.of(ALICE), true));
	}

	@Test
	void aBookRequestIsValidatedAndEveryInvalidFieldListed() throws Exception {
		mvc.perform(post("/api/v1/books").with(token(ALICE, "books:write")).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\": \" \", \"isbn\": \"\", \"publisher\": \"Ace\"}"))
				.andExpect(status().isBadRequest()).andExpect(problem("invalid-value"))
				.andExpect(jsonPath("$.errors[*].field").value(org.hamcrest.Matchers.containsInAnyOrder("title", "isbn",
						"authorId")));
		verifyNoInteractions(createBook);
	}

	@Test
	void domainValidationNamesTheFieldToo() throws Exception {
		var editor = token(ALICE, "books:write");

		mvc.perform(post("/api/v1/books").with(editor).contentType(MediaType.APPLICATION_JSON)
				.content(VALID_BOOK.replace("978-0-441-17271-9", "9780441172710")))
				.andExpect(status().isBadRequest()).andExpect(problem("invalid-value"))
				.andExpect(jsonPath("$.errors[0].field").value("isbn"));
		mvc.perform(post("/api/v1/books").with(editor).contentType(MediaType.APPLICATION_JSON)
				.content(VALID_BOOK.replace(AUTHOR, "not-a-uuid")))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("authorId"));
		mvc.perform(post("/api/v1/books").with(editor).contentType(MediaType.APPLICATION_JSON).content("{not json"))
				.andExpect(status().isBadRequest()).andExpect(problem("invalid-value"));
		mvc.perform(get("/api/v1/books/not-a-uuid").with(token(ALICE, "books:read")))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("id"));
		verifyNoInteractions(createBook, getBook);
	}

	@Test
	void updateDeleteAndTransferPassTheCallerToTheUseCases() throws Exception {
		given(updateBook.handle(eq(BookId.of(BOOK)), any(), any())).willReturn(view(ALICE));
		given(transferBook.handle(eq(BookId.of(BOOK)), eq(OwnerId.of(BOB)), any())).willReturn(view(BOB));

		mvc.perform(put("/api/v1/books/" + BOOK).with(token(ALICE, "books:write")).contentType(MediaType.APPLICATION_JSON)
				.content(VALID_BOOK)).andExpect(status().isOk()).andExpect(jsonPath("$.ownerId").value(ALICE));
		mvc.perform(delete("/api/v1/books/" + BOOK).with(token(ALICE, "books:write")))
				.andExpect(status().isNoContent()).andExpect(content().string(""));
		mvc.perform(put("/api/v1/books/" + BOOK + "/owner").with(token(ALICE, "books:manage"))
				.contentType(MediaType.APPLICATION_JSON).content("{\"ownerId\": \"" + BOB + "\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.ownerId").value(BOB));

		verify(deleteBook).handle(BookId.of(BOOK), new CatalogActor(OwnerId.of(ALICE), false));
		verify(transferBook).handle(BookId.of(BOOK), OwnerId.of(BOB), new CatalogActor(OwnerId.of(ALICE), true));
	}

	@Test
	void everyCatalogErrorHasItsOwnStatusAndStableCode() throws Exception {
		var editor = token(ALICE, "books:read", "books:write", "books:manage", "authors:manage");
		String transfer = "{\"ownerId\": \"" + BOB + "\"}";

		given(getBook.handle(any())).willThrow(new BookNotFoundException("No book with ID " + BOOK));
		mvc.perform(get("/api/v1/books/" + BOOK).with(editor)).andExpect(status().isNotFound())
				.andExpect(problem("book-not-found")).andExpect(jsonPath("$.detail").value("No book with ID " + BOOK));

		given(createBook.handle(any(), any())).willThrow(new DuplicateBookException("Another book already has ISBN 9780441172719"));
		mvc.perform(post("/api/v1/books").with(editor).contentType(MediaType.APPLICATION_JSON).content(VALID_BOOK))
				.andExpect(status().isConflict()).andExpect(problem("duplicate-book"));

		given(updateBook.handle(any(), any(), any()))
				.willThrow(new OperationNotPermittedException("A book may only be changed by its owner or a catalog manager"));
		mvc.perform(put("/api/v1/books/" + BOOK).with(editor).contentType(MediaType.APPLICATION_JSON).content(VALID_BOOK))
				.andExpect(status().isForbidden()).andExpect(problem("operation-not-permitted"));

		given(transferBook.handle(any(), any(), any())).willThrow(new OwnerNotEligibleException("No user with ID " + BOB));
		mvc.perform(put("/api/v1/books/" + BOOK + "/owner").with(editor).contentType(MediaType.APPLICATION_JSON)
				.content(transfer)).andExpect(status().isUnprocessableContent()).andExpect(problem("owner-not-eligible"));

		given(getAuthor.handle(any())).willThrow(new AuthorNotFoundException("No author with ID " + AUTHOR));
		mvc.perform(get("/api/v1/authors/" + AUTHOR).with(editor)).andExpect(status().isNotFound())
				.andExpect(problem("author-not-found"));

		willThrow(new AuthorInUseException("Author " + AUTHOR + " still has books")).given(deleteAuthor).handle(any());
		mvc.perform(delete("/api/v1/authors/" + AUTHOR).with(editor)).andExpect(status().isConflict())
				.andExpect(problem("author-in-use"));
	}

	@Test
	void platformFailuresAndUnexpectedErrorsRevealNothingInternal() throws Exception {
		var manager = token(ALICE, "books:read", "books:manage");
		given(transferBook.handle(any(), any(), any())).willThrow(new UserDirectoryUnavailableException(
				"The user directory could not be asked about a user", new IllegalStateException("secret-internal-detail")));
		given(getBook.handle(any())).willThrow(new IllegalStateException("jdbc:postgresql://secret-internal-detail"));

		mvc.perform(put("/api/v1/books/" + BOOK + "/owner").with(manager).contentType(MediaType.APPLICATION_JSON)
				.content("{\"ownerId\": \"" + BOB + "\"}"))
				.andExpect(status().isServiceUnavailable()).andExpect(problem("user-directory-unavailable"))
				.andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret-internal"))));
		mvc.perform(get("/api/v1/books/" + BOOK).with(manager))
				.andExpect(status().isInternalServerError()).andExpect(problem("internal-error"))
				.andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret-internal"))));
	}

	@Test
	void searchingBooksBuildsTheFiltersAndReturnsAPageWithTotal() throws Exception {
		given(searchBooks.handle(any())).willReturn(new PageResult<>(List.of(view(ALICE)), 41, 2, 5));

		mvc.perform(get("/api/v1/books?title=dune&publisher=ace&isbn=978-0-441-17271-9&authorId=" + AUTHOR
				+ "&ownerId=" + BOB + "&page=2&size=5&sort=createdAt,desc").with(token(ALICE, "books:read")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(41)).andExpect(jsonPath("$.page").value(2))
				.andExpect(jsonPath("$.size").value(5)).andExpect(jsonPath("$.items[0].title").value("Dune"));

		verify(searchBooks).handle(new BookSearch("dune", new Isbn("9780441172719"), "ace", AuthorId.of(AUTHOR),
				OwnerId.of(BOB), new Paging(2, 5, "createdAt", false)));
	}

	@Test
	void ownerMeLimitsTheSearchToTheCallersOwnBooks() throws Exception {
		given(searchBooks.handle(any())).willReturn(new PageResult<>(List.of(), 0, 0, 20));

		mvc.perform(get("/api/v1/books?owner=me").with(token(ALICE, "books:read"))).andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isEmpty());

		verify(searchBooks).handle(new BookSearch(null, null, null, null, OwnerId.of(ALICE), new Paging(0, 20, "title", true)));
	}

	@Test
	void badSearchParametersAreRejected() throws Exception {
		var reader = token(ALICE, "books:read");

		for (String query : List.of("sort=isbn", "sort=title,sideways", "size=0", "size=101", "page=-1", "owner=bob",
				"ownerId=nope", "authorId=nope", "isbn=123", "page=x")) {
			mvc.perform(get("/api/v1/books?" + query).with(reader)).andExpect(status().isBadRequest())
					.andExpect(problem("invalid-value"));
		}
		mvc.perform(get("/api/v1/authors?sort=title").with(reader)).andExpect(status().isBadRequest());
		verifyNoInteractions(searchBooks, searchAuthors);
	}

	// --- authors

	@Test
	void aCatalogManagerCreatesUpdatesAndDeletesAuthors() throws Exception {
		var manager = token(ALICE, "authors:manage");
		given(createAuthor.handle(any(), any())).willReturn(author());
		given(updateAuthor.handle(eq(AuthorId.of(AUTHOR)), any(), any())).willReturn(author());

		mvc.perform(post("/api/v1/authors").with(manager).contentType(MediaType.APPLICATION_JSON).content(VALID_AUTHOR))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/authors/" + AUTHOR))
				.andExpect(jsonPath("$.firstName").value("Frank")).andExpect(jsonPath("$.genre").value("Science Fiction"));
		mvc.perform(put("/api/v1/authors/" + AUTHOR).with(manager).contentType(MediaType.APPLICATION_JSON)
				.content(VALID_AUTHOR)).andExpect(status().isOk());
		mvc.perform(delete("/api/v1/authors/" + AUTHOR).with(manager)).andExpect(status().isNoContent());

		verify(createAuthor).handle(new PersonName("Frank", "Herbert"), new Genre("Science Fiction"));
		verify(updateAuthor).handle(AuthorId.of(AUTHOR), new PersonName("Frank", "Herbert"), new Genre("Science Fiction"));
		verify(deleteAuthor).handle(AuthorId.of(AUTHOR));
	}

	@Test
	void anAuthorRequestIsValidated() throws Exception {
		mvc.perform(post("/api/v1/authors").with(token(ALICE, "authors:manage")).contentType(MediaType.APPLICATION_JSON)
				.content("{\"firstName\": \"\", \"lastName\": \"Herbert\"}"))
				.andExpect(status().isBadRequest()).andExpect(problem("invalid-value"))
				.andExpect(jsonPath("$.errors[*].field").value(org.hamcrest.Matchers.containsInAnyOrder("firstName", "genre")));
		verifyNoInteractions(createAuthor);
	}

	@Test
	void searchingAuthorsBuildsTheFilters() throws Exception {
		given(searchAuthors.handle(any())).willReturn(new PageResult<>(List.of(author()), 1, 0, 10));

		mvc.perform(get("/api/v1/authors?name=herb&genre=fiction&size=10&sort=genre,desc").with(token(ALICE, "books:read")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.items[0].lastName").value("Herbert"));

		verify(searchAuthors).handle(new AuthorSearch("herb", "fiction", new Paging(0, 10, "genre", false)));
	}

	// --- helpers

	private static JwtRequestPostProcessor token(String subject, String... permissions) {
		return jwt().jwt(token -> token.subject(subject)
				.claim("realm_access", Map.of("roles", List.of("USER")))
				.claim("permissions", List.of(permissions)))
				.authorities(new PlatformAuthoritiesConverter());
	}

	private static ResultMatcher problem(String code) {
		return result -> {
			content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON).match(result);
			jsonPath("$.code").value(code).match(result);
			jsonPath("$.type").value(ProblemResponses.TYPE_BASE + code).match(result);
		};
	}

	private static Author author() {
		return Author.rehydrate(AuthorId.of(AUTHOR), new PersonName("Frank", "Herbert"), new Genre("Science Fiction"),
				CREATED, CREATED);
	}

	private static BookView view(String owner) {
		var details = new BookDetails(new Title("Dune"), "A desert planet.", new Isbn("9780441172719"),
				new Publisher("Ace"), AuthorId.of(AUTHOR), false);
		return new BookView(Book.rehydrate(BookId.of(BOOK), details, new OwnerId(UUID.fromString(owner)), CREATED, CREATED),
				author());
	}
}
