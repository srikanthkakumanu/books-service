package com.books.infrastructure.config;

import java.time.Clock;

import com.books.application.CreateAuthor;
import com.books.application.CreateBook;
import com.books.application.DeleteAuthor;
import com.books.application.DeleteBook;
import com.books.application.GetAuthor;
import com.books.application.GetBook;
import com.books.application.SearchAuthors;
import com.books.application.SearchBooks;
import com.books.application.SeedCatalog;
import com.books.application.TransferBook;
import com.books.application.UpdateAuthor;
import com.books.application.UpdateBook;
import com.books.domain.port.AuthorRepository;
import com.books.domain.port.BookRepository;
import com.books.domain.port.UserDirectoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the use cases. They are plain classes with no framework annotations, so the application
 * layer depends on nothing but the domain.
 */
@Configuration(proxyBeanMethods = false)
class UseCaseConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	CreateBook createBook(BookRepository books, AuthorRepository authors, UserDirectoryPort directory, Clock clock) {
		return new CreateBook(books, authors, directory, clock);
	}

	@Bean
	UpdateBook updateBook(BookRepository books, AuthorRepository authors, Clock clock) {
		return new UpdateBook(books, authors, clock);
	}

	@Bean
	DeleteBook deleteBook(BookRepository books) {
		return new DeleteBook(books);
	}

	@Bean
	TransferBook transferBook(BookRepository books, AuthorRepository authors, UserDirectoryPort directory, Clock clock) {
		return new TransferBook(books, authors, directory, clock);
	}

	@Bean
	GetBook getBook(BookRepository books, AuthorRepository authors) {
		return new GetBook(books, authors);
	}

	@Bean
	SearchBooks searchBooks(BookRepository books, AuthorRepository authors) {
		return new SearchBooks(books, authors);
	}

	@Bean
	CreateAuthor createAuthor(AuthorRepository authors, Clock clock) {
		return new CreateAuthor(authors, clock);
	}

	@Bean
	UpdateAuthor updateAuthor(AuthorRepository authors, Clock clock) {
		return new UpdateAuthor(authors, clock);
	}

	@Bean
	DeleteAuthor deleteAuthor(AuthorRepository authors, BookRepository books) {
		return new DeleteAuthor(authors, books);
	}

	@Bean
	GetAuthor getAuthor(AuthorRepository authors) {
		return new GetAuthor(authors);
	}

	@Bean
	SearchAuthors searchAuthors(AuthorRepository authors) {
		return new SearchAuthors(authors);
	}

	@Bean
	SeedCatalog seedCatalog(AuthorRepository authors, BookRepository books, Clock clock) {
		return new SeedCatalog(authors, books, clock);
	}
}
