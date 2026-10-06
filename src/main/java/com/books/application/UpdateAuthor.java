package com.books.application;

import java.time.Clock;

import com.books.domain.model.Author;
import com.books.domain.model.AuthorId;
import com.books.domain.model.Genre;
import com.books.domain.model.PersonName;
import com.books.domain.port.AuthorRepository;

public class UpdateAuthor {

	private final AuthorRepository authors;
	private final Clock clock;

	public UpdateAuthor(AuthorRepository authors, Clock clock) {
		this.authors = authors;
		this.clock = clock;
	}

	public Author handle(AuthorId id, PersonName name, Genre genre) {
		return authors.save(Catalog.author(authors, id).revise(name, genre, clock.instant()));
	}
}
