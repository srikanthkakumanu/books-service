package com.books.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Persistence shape of a book. Kept apart from the domain {@code Book}. */
@Entity
@Table(name = "book")
class BookJpaEntity {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "description")
	private String description;

	@Column(name = "isbn", nullable = false)
	private String isbn;

	@Column(name = "publisher", nullable = false)
	private String publisher;

	@Column(name = "author_id", nullable = false)
	private UUID authorId;

	@Column(name = "owner_id")
	private UUID ownerId;

	@Column(name = "completed", nullable = false)
	private boolean completed;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	protected BookJpaEntity() {
	}

	BookJpaEntity(UUID id, Instant createdAt) {
		this.id = id;
		this.createdAt = createdAt;
	}

	void apply(String title, String description, String isbn, String publisher, UUID authorId, UUID ownerId,
			boolean completed, Instant updatedAt) {
		this.title = title;
		this.description = description;
		this.isbn = isbn;
		this.publisher = publisher;
		this.authorId = authorId;
		this.ownerId = ownerId;
		this.completed = completed;
		this.updatedAt = updatedAt;
	}

	UUID getId() {
		return id;
	}

	String getTitle() {
		return title;
	}

	String getDescription() {
		return description;
	}

	String getIsbn() {
		return isbn;
	}

	String getPublisher() {
		return publisher;
	}

	UUID getAuthorId() {
		return authorId;
	}

	UUID getOwnerId() {
		return ownerId;
	}

	boolean isCompleted() {
		return completed;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getUpdatedAt() {
		return updatedAt;
	}
}
