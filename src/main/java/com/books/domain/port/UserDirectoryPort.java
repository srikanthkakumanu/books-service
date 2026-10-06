package com.books.domain.port;

import com.books.domain.model.OwnerId;
import com.books.domain.model.UserStanding;

/** Asks the platform about a user. Users are managed elsewhere; the catalog only refers to them. */
public interface UserDirectoryPort {

	/** @throws com.books.domain.exception.UserDirectoryUnavailableException if the platform cannot be asked */
	UserStanding standingOf(OwnerId userId);
}
