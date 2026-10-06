package com.books.domain.model;

import java.time.Instant;
import java.util.Objects;

/** An author in the catalog. */
public final class Author {

	private final AuthorId id;
	private final PersonName name;
	private final Genre genre;
	private final Instant createdAt;
	private final Instant updatedAt;

	private Author(AuthorId id, PersonName name, Genre genre, Instant createdAt, Instant updatedAt) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = Objects.requireNonNull(name, "name");
		this.genre = Objects.requireNonNull(genre, "genre");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
		this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
	}

	public static Author create(AuthorId id, PersonName name, Genre genre, Instant now) {
		return new Author(id, name, genre, now, now);
	}

	/** Rebuilds an author from storage. */
	public static Author rehydrate(AuthorId id, PersonName name, Genre genre, Instant createdAt, Instant updatedAt) {
		return new Author(id, name, genre, createdAt, updatedAt);
	}

	public Author revise(PersonName name, Genre genre, Instant now) {
		return new Author(id, name, genre, createdAt, now);
	}

	public AuthorId id() {
		return id;
	}

	public PersonName name() {
		return name;
	}

	public Genre genre() {
		return genre;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
