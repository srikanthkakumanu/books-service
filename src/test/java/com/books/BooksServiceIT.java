package com.books;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.books.support.StubPlatform;
import com.books.support.TestIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The whole service over HTTP with real Postgres, the real seed files, real signature validation
 * against a JWKS endpoint, and stand-ins for auth-service and user-service.
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = { "books.seed.enabled=true",
		"platform.directory.load-balanced=false" })
class BooksServiceIT {

	private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {
	};
	private static final List<String> READER = List.of("books:read");
	private static final List<String> EDITOR = List.of("books:read", "books:write");
	private static final List<String> MANAGER = List.of("books:read", "books:write", "books:manage", "authors:manage");

	static final TestIssuer issuer = new TestIssuer();
	static final StubPlatform platform = new StubPlatform();

	@Container
	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private ApplicationRunner catalogSeeder;

	private final String alice = UUID.randomUUID().toString();
	private final String bob = UUID.randomUUID().toString();

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("platform.security.jwt.issuer-uri", () -> TestIssuer.ISSUER);
		registry.add("platform.security.jwt.jwk-set-uri", issuer::jwkSetUri);
		registry.add("platform.directory.user-service-url", platform::url);
		registry.add("platform.directory.auth-service-url", platform::url);
		registry.add("platform.directory.client-secret", () -> StubPlatform.CLIENT_SECRET);
	}

	@BeforeEach
	void knownUsers() {
		platform.reset();
		platform.user(alice, "ACTIVE");
		platform.user(bob, "ACTIVE");
	}

	@Test
	void theSeedFilesAreLoadedCompletelyAndLoadingAgainChangesNothing() throws Exception {
		assertThat(count("select count(*) from author")).isEqualTo(47);
		assertThat(count("select count(*) from book")).isEqualTo(44);
		assertThat(count("select count(*) from book b where not exists (select 1 from author a where a.id = b.author_id)"))
				.isZero();
		assertThat(count("select count(distinct isbn) from book where owner_id is null")).isEqualTo(44);
		assertThat(count("select count(*) from book where owner_id is null and description is null")).isZero();
		long version = count("select coalesce(sum(version), 0) from book where owner_id is null");

		catalogSeeder.run(new DefaultApplicationArguments());

		assertThat(count("select count(*) from author")).isEqualTo(47);
		assertThat(count("select count(*) from book where owner_id is null")).isEqualTo(44);
		assertThat(count("select coalesce(sum(version), 0) from book where owner_id is null")).isEqualTo(version);
	}

	@Test
	void nothingIsUsableWithoutLoggingInAndARoleIsNeededOnTop() {
		assertProblem(call(HttpMethod.GET, "/api/v1/books", null, null), 401, "unauthorized");
		assertProblem(call(HttpMethod.GET, "/api/v1/authors", null, null), 401, "unauthorized");
		assertProblem(call(HttpMethod.GET, "/api/v1/books", "not.a.token", null), 401, "unauthorized");
		assertProblem(call(HttpMethod.GET, "/api/v1/books", issuer.token(claims -> claims.audience("user-service")
				.claim("permissions", MANAGER)), null), 401, "unauthorized");

		// Logged in, but holding only the platform's default USER role: no catalog permission.
		String plainUser = issuer.token(alice, List.of("USER"), List.of());
		assertProblem(call(HttpMethod.GET, "/api/v1/books", plainUser, null), 403, "forbidden");
		assertProblem(call(HttpMethod.GET, "/api/v1/authors", plainUser, null), 403, "forbidden");

		assertThat(call(HttpMethod.GET, "/actuator/health/readiness", null, null).getStatusCode().value()).isEqualTo(200);
		assertThat(call(HttpMethod.GET, "/v3/api-docs", null, null).getStatusCode().value()).isEqualTo(200);
	}

	@Test
	void aReaderBrowsesTheSeededCatalogWithAuthors() {
		String reader = issuer.token(alice, List.of("USER", "CATALOG_READER"), READER);

		var firstPage = call(HttpMethod.GET, "/api/v1/books?size=5&sort=title,asc", reader, null);
		assertThat(firstPage.getStatusCode().value()).isEqualTo(200);
		assertThat(((Number) firstPage.getBody().get("total")).intValue()).isGreaterThanOrEqualTo(44);
		assertThat(items(firstPage)).hasSize(5).allSatisfy(book -> {
			assertThat(book.get("isbn")).asString().matches("97[89][0-9]{10}");
			assertThat(book.get("author")).asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
					.containsKeys("id", "firstName", "lastName");
		});

		var enigma = call(HttpMethod.GET, "/api/v1/books?title=enigma%20of%20elysium", reader, null);
		assertThat(items(enigma)).singleElement().satisfies(book -> {
			assertThat(book).containsEntry("title", "The Enigma of Elysium").containsEntry("publisher", "Mystic Press")
					.containsEntry("ownerId", null);
			assertThat(book.get("author")).asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
					.containsEntry("firstName", "Evelyn").containsEntry("lastName", "Wren");
		});

		var mysteryAuthors = call(HttpMethod.GET, "/api/v1/authors?genre=mystery&sort=lastName", reader, null);
		assertThat(items(mysteryAuthors)).isNotEmpty().allSatisfy(
				author -> assertThat(author.get("genre")).asString().containsIgnoringCase("mystery"));
		assertThat(((Number) call(HttpMethod.GET, "/api/v1/authors?size=1", reader, null).getBody().get("total")).intValue())
				.isGreaterThanOrEqualTo(47);

		String seededId = (String) items(enigma).getFirst().get("id");
		assertProblem(call(HttpMethod.POST, "/api/v1/books", reader, bookBody("Mine", isbn(), anAuthorId(reader))), 403,
				"forbidden");
		assertProblem(call(HttpMethod.DELETE, "/api/v1/books/" + seededId, reader, null), 403, "forbidden");
	}

	@Test
	void anEditorOwnsWhatTheyAddAndNobodyElseMayChangeIt() {
		String aliceToken = issuer.token(alice, List.of("USER", "CATALOG_EDITOR"), EDITOR);
		String bobToken = issuer.token(bob, List.of("USER", "CATALOG_EDITOR"), EDITOR);
		String authorId = anAuthorId(aliceToken);
		String isbn = isbn();
		int lookupsBefore = platform.authorizationHeaders().size();

		var created = call(HttpMethod.POST, "/api/v1/books", aliceToken, bookBody("Alice's Notebook", isbn, authorId));
		String id = (String) created.getBody().get("id");
		assertThat(created.getStatusCode().value()).isEqualTo(201);
		assertThat(created.getHeaders().getLocation()).hasToString("/api/v1/books/" + id);
		assertThat(created.getBody()).containsEntry("ownerId", alice).containsEntry("completed", false);
		assertThat(jdbc.queryForObject("select owner_id::text from book where id = ?::uuid", String.class, id))
				.isEqualTo(alice);

		assertProblem(call(HttpMethod.POST, "/api/v1/books", bobToken, bookBody("Copycat", isbn, authorId)), 409,
				"duplicate-book");
		assertProblem(call(HttpMethod.PUT, "/api/v1/books/" + id, bobToken, bookBody("Hijacked", isbn, authorId)), 403,
				"operation-not-permitted");
		assertProblem(call(HttpMethod.DELETE, "/api/v1/books/" + id, bobToken, null), 403, "operation-not-permitted");
		// An editor cannot add a book in someone else's name, nor touch the seeded catalog or authors.
		var forBob = new java.util.HashMap<>(bookBody("For Bob", isbn(), authorId));
		forBob.put("ownerId", bob);
		assertProblem(call(HttpMethod.POST, "/api/v1/books", aliceToken, forBob), 403, "operation-not-permitted");
		String seededId = (String) items(call(HttpMethod.GET, "/api/v1/books?title=Whispers%20in%20the%20Mist",
				aliceToken, null)).getFirst().get("id");
		assertProblem(call(HttpMethod.DELETE, "/api/v1/books/" + seededId, aliceToken, null), 403,
				"operation-not-permitted");
		assertProblem(call(HttpMethod.POST, "/api/v1/authors", aliceToken,
				Map.of("firstName", "New", "lastName", "Author", "genre", "Poetry")), 403, "forbidden");

		var updated = call(HttpMethod.PUT, "/api/v1/books/" + id, aliceToken, Map.of("title", "Alice's Second Notebook",
				"isbn", isbn, "publisher", "Home Press", "authorId", authorId, "completed", true));
		assertThat(updated.getBody()).containsEntry("title", "Alice's Second Notebook").containsEntry("completed", true)
				.containsEntry("ownerId", alice);

		var mine = call(HttpMethod.GET, "/api/v1/books?owner=me", aliceToken, null);
		assertThat(items(mine)).extracting(book -> book.get("id")).containsExactly(id);
		assertThat(items(call(HttpMethod.GET, "/api/v1/books?owner=me", bobToken, null))).isEmpty();

		assertThat(call(HttpMethod.DELETE, "/api/v1/books/" + id, aliceToken, null).getStatusCode().value()).isEqualTo(204);
		assertProblem(call(HttpMethod.GET, "/api/v1/books/" + id, aliceToken, null), 404, "book-not-found");
		assertThat(platform.authorizationHeaders()).as("an editor's own books need no user lookup").hasSize(lookupsBefore);
	}

	@Test
	void aManagerTransfersBooksOnlyToActivePlatformUsersAndManagesAuthors() {
		String manager = issuer.token(UUID.randomUUID().toString(), List.of("USER", "CATALOG_MANAGER"), MANAGER);
		String aliceToken = issuer.token(alice, List.of("USER", "CATALOG_EDITOR"), EDITOR);
		String disabled = UUID.randomUUID().toString();
		platform.user(disabled, "DISABLED");

		var author = call(HttpMethod.POST, "/api/v1/authors", manager,
				Map.of("firstName", "Ursula", "lastName", "Le Guin", "genre", "Science Fiction"));
		String authorId = (String) author.getBody().get("id");
		assertThat(author.getStatusCode().value()).isEqualTo(201);
		assertThat(author.getHeaders().getLocation()).hasToString("/api/v1/authors/" + authorId);
		assertThat(call(HttpMethod.PUT, "/api/v1/authors/" + authorId, manager,
				Map.of("firstName", "Ursula K.", "lastName", "Le Guin", "genre", "Speculative Fiction")).getBody())
				.containsEntry("firstName", "Ursula K.").containsEntry("genre", "Speculative Fiction");

		String id = (String) call(HttpMethod.POST, "/api/v1/books", aliceToken,
				bookBody("The Dispossessed", isbn(), authorId)).getBody().get("id");

		assertProblem(call(HttpMethod.PUT, "/api/v1/books/" + id + "/owner", aliceToken, Map.of("ownerId", bob)), 403,
				"forbidden");
		assertProblem(call(HttpMethod.PUT, "/api/v1/books/" + id + "/owner", manager,
				Map.of("ownerId", UUID.randomUUID().toString())), 422, "owner-not-eligible");
		assertProblem(call(HttpMethod.PUT, "/api/v1/books/" + id + "/owner", manager, Map.of("ownerId", disabled)), 422,
				"owner-not-eligible");
		platform.failUserLookupsWith(503);
		assertProblem(call(HttpMethod.PUT, "/api/v1/books/" + id + "/owner", manager, Map.of("ownerId", bob)), 503,
				"user-directory-unavailable");
		platform.failUserLookupsWith(0);
		assertThat(call(HttpMethod.GET, "/api/v1/books/" + id, manager, null).getBody()).containsEntry("ownerId", alice);

		assertThat(call(HttpMethod.PUT, "/api/v1/books/" + id + "/owner", manager, Map.of("ownerId", bob)).getBody())
				.containsEntry("ownerId", bob);
		// The lookup was made as this service, with its own token, not with the caller's.
		assertThat(platform.authorizationHeaders()).isNotEmpty().allMatch(header -> header.startsWith("Bearer service-token-"));
		// The previous owner has lost the book; the new one has it.
		assertProblem(call(HttpMethod.DELETE, "/api/v1/books/" + id, aliceToken, null), 403, "operation-not-permitted");

		assertProblem(call(HttpMethod.DELETE, "/api/v1/authors/" + authorId, manager, null), 409, "author-in-use");
		String bobToken = issuer.token(bob, List.of("USER", "CATALOG_EDITOR"), EDITOR);
		assertThat(call(HttpMethod.DELETE, "/api/v1/books/" + id, bobToken, null).getStatusCode().value()).isEqualTo(204);
		assertThat(call(HttpMethod.DELETE, "/api/v1/authors/" + authorId, manager, null).getStatusCode().value())
				.isEqualTo(204);
		assertProblem(call(HttpMethod.GET, "/api/v1/authors/" + authorId, manager, null), 404, "author-not-found");
	}

	@Test
	void aManagerChangesSeededBooksAndAddsBooksForOtherUsers() {
		String manager = issuer.token(UUID.randomUUID().toString(), List.of("USER", "CATALOG_MANAGER"), MANAGER);
		String authorId = anAuthorId(manager);

		var forBob = new java.util.HashMap<>(bookBody("A Gift for Bob", isbn(), authorId));
		forBob.put("ownerId", bob);
		var created = call(HttpMethod.POST, "/api/v1/books", manager, forBob);
		assertThat(created.getStatusCode().value()).isEqualTo(201);
		assertThat(created.getBody()).containsEntry("ownerId", bob);

		var seeded = items(call(HttpMethod.GET, "/api/v1/books?title=Chronicles%20of%20Nebula", manager, null)).getFirst();
		var revised = call(HttpMethod.PUT, "/api/v1/books/" + seeded.get("id"), manager,
				Map.of("title", "Chronicles of Nebula", "description", "Revised by the catalog team.", "isbn",
						seeded.get("isbn"), "publisher", seeded.get("publisher"), "authorId", authorId));
		assertThat(revised.getBody()).containsEntry("description", "Revised by the catalog team.")
				.containsEntry("ownerId", null);

		assertThat(call(HttpMethod.DELETE, "/api/v1/books/" + created.getBody().get("id"), manager, null).getStatusCode()
				.value()).isEqualTo(204);
	}

	// --- helpers

	private long count(String sql) {
		return jdbc.queryForObject(sql, Long.class);
	}

	private String anAuthorId(String token) {
		return (String) items(call(HttpMethod.GET, "/api/v1/authors?size=1", token, null)).getFirst().get("id");
	}

	/** A fresh valid ISBN-13 in the 979 range, which the seed data does not use. */
	private static String isbn() {
		String twelve = "979" + String.format("%09d", (long) (Math.random() * 1_000_000_000L));
		return twelve + com.books.domain.model.Isbn.checkDigit(twelve);
	}

	private static Map<String, Object> bookBody(String title, String isbn, String authorId) {
		return Map.of("title", title, "isbn", isbn, "publisher", "Test Press", "authorId", authorId);
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> items(ResponseEntity<Map<String, Object>> response) {
		return (List<Map<String, Object>>) response.getBody().get("items");
	}

	private static void assertProblem(ResponseEntity<Map<String, Object>> response, int status, String code) {
		assertThat(response.getStatusCode().value()).isEqualTo(status);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(response.getBody()).containsEntry("code", code)
				.containsEntry("type", "https://platform.local/problems/" + code);
	}

	private ResponseEntity<Map<String, Object>> call(HttpMethod method, String path, String token, Object body) {
		var request = RestClient.create("http://localhost:" + port).method(method).uri(java.net.URI.create(
				"http://localhost:" + port + path));
		if (token != null) {
			request.header("Authorization", "Bearer " + token);
		}
		if (body != null) {
			request.contentType(MediaType.APPLICATION_JSON).body(body);
		}
		return request.exchange((req, res) -> {
			Map<String, Object> parsed = res.getHeaders().getContentLength() == 0 || res.getStatusCode().value() == 204
					? null : res.bodyTo(JSON);
			return ResponseEntity.status(res.getStatusCode()).headers(res.getHeaders()).body(parsed);
		});
	}
}
