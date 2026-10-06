package com.books.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.books.domain.exception.OperationNotPermittedException;

/**
 * A book in the catalog. A book with an owner is changed only by that owner or by someone who
 * manages every book; a book without an owner belongs to the catalog and is changed only by the
 * latter.
 */
public final class Book {

	private final BookId id;
	private final BookDetails details;
	private final OwnerId ownerId;
	private final Instant createdAt;
	private final Instant updatedAt;

	private Book(BookId id, BookDetails details, OwnerId ownerId, Instant createdAt, Instant updatedAt) {
		this.id = Objects.requireNonNull(id, "id");
		this.details = Objects.requireNonNull(details, "details");
		this.ownerId = ownerId;
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
		this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
	}

	/** A book added by a user. It belongs to them unless a manager adds it for someone else. */
	public static Book create(BookId id, BookDetails details, OwnerId requestedOwner, CatalogActor actor, Instant now) {
		OwnerId owner = requestedOwner == null ? actor.userId() : requestedOwner;
		if (!actor.is(owner) && !actor.managesAllBooks()) {
			throw new OperationNotPermittedException("Only a catalog manager may add a book for someone else");
		}
		return new Book(id, details, owner, now, now);
	}

	/** A book that belongs to the catalog itself, such as seed data. */
	public static Book catalogOwned(BookId id, BookDetails details, Instant now) {
		return new Book(id, details, null, now, now);
	}

	/** Rebuilds a book from storage. */
	public static Book rehydrate(BookId id, BookDetails details, OwnerId ownerId, Instant createdAt, Instant updatedAt) {
		return new Book(id, details, ownerId, createdAt, updatedAt);
	}

	public Book revise(BookDetails details, CatalogActor actor, Instant now) {
		requireWriteAccess(actor);
		return new Book(id, details, ownerId, createdAt, now);
	}

	public Book transferTo(OwnerId newOwner, CatalogActor actor, Instant now) {
		Objects.requireNonNull(newOwner, "newOwner");
		if (!actor.managesAllBooks()) {
			throw new OperationNotPermittedException("Only a catalog manager may transfer a book");
		}
		return new Book(id, details, newOwner, createdAt, now);
	}

	public void requireWriteAccess(CatalogActor actor) {
		if (!actor.managesAllBooks() && !isOwnedBy(actor.userId())) {
			throw new OperationNotPermittedException("A book may only be changed by its owner or a catalog manager");
		}
	}

	public boolean isOwnedBy(OwnerId user) {
		return ownerId != null && ownerId.equals(user);
	}

	public BookId id() {
		return id;
	}

	public BookDetails details() {
		return details;
	}

	public Optional<OwnerId> ownerId() {
		return Optional.ofNullable(ownerId);
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
