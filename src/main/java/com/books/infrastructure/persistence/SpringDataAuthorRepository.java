package com.books.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface SpringDataAuthorRepository extends JpaRepository<AuthorJpaEntity, UUID>, JpaSpecificationExecutor<AuthorJpaEntity> {
}
