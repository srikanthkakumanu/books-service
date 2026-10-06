package com.books.domain.model;

import com.books.domain.exception.InvalidValueException;

/** An ISBN-13. Hyphens and spaces are accepted on input and dropped; the check digit must be right. */
public record Isbn(String value) {

	public Isbn {
		if (value == null || value.isBlank()) {
			throw new InvalidValueException("isbn", "must not be blank");
		}
		value = value.replace("-", "").replace(" ", "");
		if (!value.matches("97[89][0-9]{10}")) {
			throw new InvalidValueException("isbn", "must be an ISBN-13: 13 digits starting with 978 or 979");
		}
		if (checkDigit(value.substring(0, 12)) != value.charAt(12) - '0') {
			throw new InvalidValueException("isbn", "has a wrong check digit");
		}
	}

	/** The check digit that completes the first twelve digits of an ISBN-13. */
	public static int checkDigit(String firstTwelveDigits) {
		int sum = 0;
		for (int i = 0; i < 12; i++) {
			int digit = firstTwelveDigits.charAt(i) - '0';
			sum += i % 2 == 0 ? digit : 3 * digit;
		}
		return (10 - sum % 10) % 10;
	}

	@Override
	public String toString() {
		return value;
	}
}
