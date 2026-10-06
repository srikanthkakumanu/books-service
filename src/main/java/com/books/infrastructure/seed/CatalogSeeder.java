package com.books.infrastructure.seed;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import com.books.application.SeedCatalog;
import com.books.application.SeedCatalog.SeedAuthor;
import com.books.application.SeedCatalog.SeedBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * Reads the seed files and hands them to {@link SeedCatalog} once the application has started and
 * the schema is migrated. Everything is loaded in one transaction: a file with an invalid entry
 * loads nothing and stops the start, so a broken seed file is noticed at once.
 */
@Component
@ConditionalOnProperty(name = "books.seed.enabled", havingValue = "true")
@EnableConfigurationProperties(SeedProperties.class)
class CatalogSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);

	record AuthorsFile(List<SeedAuthor> authors) {
	}

	record BooksFile(List<SeedBook> books) {
	}

	private final SeedProperties properties;
	private final SeedCatalog seedCatalog;
	private final ObjectMapper objectMapper;
	private final TransactionTemplate transaction;

	CatalogSeeder(SeedProperties properties, SeedCatalog seedCatalog, ObjectMapper objectMapper,
			TransactionTemplate transaction) {
		this.properties = properties;
		this.seedCatalog = seedCatalog;
		this.objectMapper = objectMapper;
		this.transaction = transaction;
	}

	@Override
	public void run(ApplicationArguments args) {
		List<SeedAuthor> authors = read(properties.authors(), AuthorsFile.class).authors();
		List<SeedBook> books = read(properties.books(), BooksFile.class).books();
		SeedCatalog.Result result = transaction.execute(status -> seedCatalog.handle(authors, books));
		log.info("Catalog seed: {} of {} authors and {} of {} books added; the rest were already present",
				result.authorsAdded(), authors.size(), result.booksAdded(), books.size());
	}

	private <T> T read(Resource resource, Class<T> type) {
		try (InputStream in = resource.getInputStream()) {
			return objectMapper.readValue(in, type);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Cannot read seed file " + resource, ex);
		}
	}
}
