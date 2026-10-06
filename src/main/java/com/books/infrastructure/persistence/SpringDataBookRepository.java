package com.books.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface SpringDataBookRepository extends JpaRepository<BookJpaEntity, UUID>, JpaSpecificationExecutor<BookJpaEntity> {

	Optional<BookJpaEntity> findByIsbn(String isbn);

	boolean existsByAuthorId(UUID authorId);
}
