package com.books.application;

import java.time.Clock;

import com.books.domain.model.Author;
import com.books.domain.model.AuthorId;
import com.books.domain.model.Genre;
import com.books.domain.model.PersonName;
import com.books.domain.port.AuthorRepository;

public class CreateAuthor {

	private final AuthorRepository authors;
	private final Clock clock;

	public CreateAuthor(AuthorRepository authors, Clock clock) {
		this.authors = authors;
		this.clock = clock;
	}

	public Author handle(PersonName name, Genre genre) {
		return authors.save(Author.create(AuthorId.newId(), name, genre, clock.instant()));
	}
}
