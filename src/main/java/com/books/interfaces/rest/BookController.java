package com.books.interfaces.rest;

import java.net.URI;

import com.books.application.CreateBook;
import com.books.application.DeleteBook;
import com.books.application.GetBook;
import com.books.application.SearchBooks;
import com.books.application.TransferBook;
import com.books.application.UpdateBook;
import com.books.domain.exception.InvalidValueException;
import com.books.domain.model.AuthorId;
import com.books.domain.model.BookId;
import com.books.domain.model.BookSearch;
import com.books.domain.model.CatalogActor;
import com.books.domain.model.Isbn;
import com.books.domain.model.OwnerId;
import com.books.domain.model.Paging;
import com.books.interfaces.rest.dto.Requests;
import com.books.interfaces.rest.dto.Responses.BookResponse;
import com.books.interfaces.rest.dto.Responses.PageResponse;
import com.books.interfaces.security.CurrentCatalogActor;
import com.books.interfaces.security.Permissions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books")
class BookController {

	private static final String ME = "me";

	private final CreateBook createBook;
	private final UpdateBook updateBook;
	private final DeleteBook deleteBook;
	private final TransferBook transferBook;
	private final GetBook getBook;
	private final SearchBooks searchBooks;

	BookController(CreateBook createBook, UpdateBook updateBook, DeleteBook deleteBook, TransferBook transferBook,
			GetBook getBook, SearchBooks searchBooks) {
		this.createBook = createBook;
		this.updateBook = updateBook;
		this.deleteBook = deleteBook;
		this.transferBook = transferBook;
		this.getBook = getBook;
		this.searchBooks = searchBooks;
	}

	@GetMapping
	@PreAuthorize(Permissions.READ)
	@Operation(summary = "Search books; owner=me limits the result to the caller's own books")
	PageResponse<BookResponse> search(@RequestParam(required = false) String title,
			@RequestParam(required = false) String isbn, @RequestParam(required = false) String publisher,
			@RequestParam(required = false) String authorId, @RequestParam(required = false) String ownerId,
			@RequestParam(required = false) String owner, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String sort,
			@AuthenticationPrincipal Jwt jwt) {
		var search = new BookSearch(title, isBlank(isbn) ? null : new Isbn(isbn), publisher,
				isBlank(authorId) ? null : AuthorId.of(authorId), owner(owner, ownerId, jwt),
				Paging.of(page, size, sort, BookSearch.DEFAULT_SORT, BookSearch.SORT_FIELDS));
		return PageResponse.from(searchBooks.handle(search).map(BookResponse::from));
	}

	@GetMapping("/{id}")
	@PreAuthorize(Permissions.READ)
	@Operation(summary = "Read a book")
	BookResponse get(@PathVariable String id) {
		return BookResponse.from(getBook.handle(BookId.of(id)));
	}

	@PostMapping
	@PreAuthorize(Permissions.WRITE)
	@Operation(summary = "Add a book; it belongs to the caller unless a catalog manager names another owner")
	ResponseEntity<BookResponse> create(@Valid @RequestBody Requests.CreateBook request,
			@AuthenticationPrincipal Jwt jwt) {
		var created = BookResponse.from(createBook.handle(new CreateBook.Command(request.details(), request.owner()),
				actor(jwt)));
		return ResponseEntity.created(URI.create("/api/v1/books/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@PreAuthorize(Permissions.WRITE)
	@Operation(summary = "Replace a book's details; only its owner or a catalog manager may")
	BookResponse update(@PathVariable String id, @Valid @RequestBody Requests.UpdateBook request,
			@AuthenticationPrincipal Jwt jwt) {
		return BookResponse.from(updateBook.handle(BookId.of(id), request.details(), actor(jwt)));
	}

	@PutMapping("/{id}/owner")
	@PreAuthorize(Permissions.MANAGE_BOOKS)
	@Operation(summary = "Give a book to another active platform user")
	BookResponse transfer(@PathVariable String id, @Valid @RequestBody Requests.TransferBook request,
			@AuthenticationPrincipal Jwt jwt) {
		return BookResponse.from(transferBook.handle(BookId.of(id), OwnerId.of(request.ownerId()), actor(jwt)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(Permissions.WRITE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove a book; only its owner or a catalog manager may")
	void delete(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
		deleteBook.handle(BookId.of(id), actor(jwt));
	}

	private static CatalogActor actor(Jwt jwt) {
		return CurrentCatalogActor.from(jwt);
	}

	private static OwnerId owner(String owner, String ownerId, Jwt jwt) {
		if (!isBlank(owner)) {
			if (!ME.equals(owner)) {
				throw new InvalidValueException("owner", "must be 'me'; use ownerId for another user");
			}
			return actor(jwt).userId();
		}
		return isBlank(ownerId) ? null : OwnerId.of(ownerId);
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
