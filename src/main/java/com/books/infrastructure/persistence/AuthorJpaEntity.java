package com.books.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Persistence shape of an author. Kept apart from the domain {@code Author}. */
@Entity
@Table(name = "author")
class AuthorJpaEntity {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "first_name", nullable = false)
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "genre", nullable = false)
	private String genre;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	protected AuthorJpaEntity() {
	}

	AuthorJpaEntity(UUID id, Instant createdAt) {
		this.id = id;
		this.createdAt = createdAt;
	}

	void apply(String firstName, String lastName, String genre, Instant updatedAt) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.genre = genre;
		this.updatedAt = updatedAt;
	}

	UUID getId() {
		return id;
	}

	String getFirstName() {
		return firstName;
	}

	String getLastName() {
		return lastName;
	}

	String getGenre() {
		return genre;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getUpdatedAt() {
		return updatedAt;
	}
}
