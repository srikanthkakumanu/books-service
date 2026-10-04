package books.infrastructure.persistence;

import books.domain.model.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Testcontainers
@DataJpaTest(properties = {
        "spring.config.import=", "spring.cloud.vault.enabled=false", "spring.cloud.config.enabled=false",
        "spring.datasource.username=theuser", "spring.datasource.password=theuser",
        "spring.flyway.user=bookadmin", "spring.flyway.password=bookadmin", "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({JpaBookStore.class, BookPersistenceMapperImpl.class, JpaAuthorStore.class, AuthorPersistenceMapperImpl.class})
class CatalogPersistenceTest {
    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18")
            .withDatabaseName("booksdb").withUsername("root").withPassword("root")
            .withInitScript("postgres-test-users.sql");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Autowired JpaBookStore books;
    @Autowired JpaAuthorStore authors;
    @Autowired jakarta.persistence.EntityManager entityManager;

    @Test
    void book_withUuidOwner_shouldRoundTripAndUpdate() {
        var owner = new CatalogActor(UUID.randomUUID(), false);
        var saved = books.save(Book.create(new BookChanges("Round trip", null, "9780000000001", "Publisher",
                null, false, null, null), owner));
        entityManager.flush();
        entityManager.clear();

        var found = books.findById(saved.id()).orElseThrow();
        assertThat(found.userId()).isEqualTo(owner.userId());
        assertThat(found.created()).isNotNull();
        books.save(found.revise(new BookChanges(null, null, null, null, null, true, null, null), owner));
        entityManager.flush();
        entityManager.clear();
        assertThat(books.findById(saved.id()).orElseThrow().completed()).isTrue();
    }

    @Test
    void authorQuery_shouldCombineFiltersAndPagination() {
        authors.save(Author.create(new AuthorChanges("Ada", "Lovelace", "Science")));
        authors.save(Author.create(new AuthorChanges("Ada", "Other", "Fiction")));
        entityManager.flush();
        entityManager.clear();

        var results = authors.find(new AuthorQuery("Ada", null, "Science", 0, 20, true, false));
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().lastName()).isEqualTo("Lovelace");
    }
}
