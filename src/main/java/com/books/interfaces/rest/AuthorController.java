package com.books.interfaces.rest;

import java.net.URI;

import com.books.application.CreateAuthor;
import com.books.application.DeleteAuthor;
import com.books.application.GetAuthor;
import com.books.application.SearchAuthors;
import com.books.application.UpdateAuthor;
import com.books.domain.model.AuthorId;
import com.books.domain.model.AuthorSearch;
import com.books.domain.model.Paging;
import com.books.interfaces.rest.dto.Requests;
import com.books.interfaces.rest.dto.Responses.AuthorResponse;
import com.books.interfaces.rest.dto.Responses.PageResponse;
import com.books.interfaces.security.Permissions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/authors")
@Tag(name = "Authors")
class AuthorController {

	private final CreateAuthor createAuthor;
	private final UpdateAuthor updateAuthor;
	private final DeleteAuthor deleteAuthor;
	private final GetAuthor getAuthor;
	private final SearchAuthors searchAuthors;

	AuthorController(CreateAuthor createAuthor, UpdateAuthor updateAuthor, DeleteAuthor deleteAuthor,
			GetAuthor getAuthor, SearchAuthors searchAuthors) {
		this.createAuthor = createAuthor;
		this.updateAuthor = updateAuthor;
		this.deleteAuthor = deleteAuthor;
		this.getAuthor = getAuthor;
		this.searchAuthors = searchAuthors;
	}

	@GetMapping
	@PreAuthorize(Permissions.READ)
	@Operation(summary = "Search authors by name and genre")
	PageResponse<AuthorResponse> search(@RequestParam(required = false) String name,
			@RequestParam(required = false) String genre, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String sort) {
		var search = new AuthorSearch(name, genre,
				Paging.of(page, size, sort, AuthorSearch.DEFAULT_SORT, AuthorSearch.SORT_FIELDS));
		return PageResponse.from(searchAuthors.handle(search).map(AuthorResponse::from));
	}

	@GetMapping("/{id}")
	@PreAuthorize(Permissions.READ)
	@Operation(summary = "Read an author")
	AuthorResponse get(@PathVariable String id) {
		return AuthorResponse.from(getAuthor.handle(AuthorId.of(id)));
	}

	@PostMapping
	@PreAuthorize(Permissions.MANAGE_AUTHORS)
	@Operation(summary = "Add an author")
	ResponseEntity<AuthorResponse> create(@Valid @RequestBody Requests.SaveAuthor request) {
		var created = AuthorResponse.from(createAuthor.handle(request.name(), request.genreValue()));
		return ResponseEntity.created(URI.create("/api/v1/authors/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	@PreAuthorize(Permissions.MANAGE_AUTHORS)
	@Operation(summary = "Replace an author's name and genre")
	AuthorResponse update(@PathVariable String id, @Valid @RequestBody Requests.SaveAuthor request) {
		return AuthorResponse.from(updateAuthor.handle(AuthorId.of(id), request.name(), request.genreValue()));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(Permissions.MANAGE_AUTHORS)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove an author who has no books")
	void delete(@PathVariable String id) {
		deleteAuthor.handle(AuthorId.of(id));
	}
}
