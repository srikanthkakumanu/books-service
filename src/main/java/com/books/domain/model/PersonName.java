package com.books.domain.model;

/** An author's name. A single-name author has no last name. */
public record PersonName(String firstName, String lastName) {

	public static final int MAX_LENGTH = 100;

	public PersonName {
		firstName = Text.required("firstName", firstName, MAX_LENGTH);
		lastName = Text.optional("lastName", lastName, MAX_LENGTH);
	}

	public String full() {
		return lastName == null ? firstName : firstName + " " + lastName;
	}
}
