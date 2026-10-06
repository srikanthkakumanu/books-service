package com.books.application;

import com.books.domain.model.Author;
import com.books.domain.model.Book;

/** A book together with its author, as callers read it. */
public record BookView(Book book, Author author) {
}
