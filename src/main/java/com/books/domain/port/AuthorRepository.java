package com.books.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.books.domain.model.Author;
import com.books.domain.model.AuthorId;
import com.books.domain.model.AuthorSearch;
import com.books.domain.model.PageResult;

public interface AuthorRepository {

	Author save(Author author);

	Optional<Author> findById(AuthorId id);

	List<Author> findByIds(Collection<AuthorId> ids);

	boolean exists(AuthorId id);

	PageResult<Author> search(AuthorSearch search);

	/** @throws com.books.domain.exception.AuthorInUseException if the author still has books */
	void delete(AuthorId id);
}
