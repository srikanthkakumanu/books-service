package com.books.interfaces.security;

import com.books.domain.model.CatalogActor;
import com.books.domain.model.OwnerId;
import com.platform.security.claims.AccessTokenClaims;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Builds the catalog's view of the caller from the validated platform access token: the user ID
 * is the token subject (the ID user-service manages), and what they may do comes from the
 * permissions auth-service put in the token.
 */
public final class CurrentCatalogActor {

	private CurrentCatalogActor() {
	}

	public static CatalogActor from(Jwt jwt) {
		var claims = AccessTokenClaims.from(jwt.getClaims());
		return new CatalogActor(OwnerId.of(claims.subject()), claims.hasPermission(Permissions.BOOKS_MANAGE));
	}
}
