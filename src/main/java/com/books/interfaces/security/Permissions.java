package com.books.interfaces.security;

/**
 * Method-security expressions used by the controllers. The permissions are granted to roles in
 * auth-service and arrive in the access token; this service defines no roles of its own.
 */
public final class Permissions {

	public static final String BOOKS_MANAGE = "books:manage";

	public static final String READ = "hasAuthority('books:read')";
	public static final String WRITE = "hasAuthority('books:write')";
	public static final String MANAGE_BOOKS = "hasAuthority('" + BOOKS_MANAGE + "')";
	public static final String MANAGE_AUTHORS = "hasAuthority('authors:manage')";

	private Permissions() {
	}
}
