package books.controller;

import books.dto.AuthorDTO;
import books.dto.SortOrder;
import books.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/authors")
@RestController
@Tag(name = "Author API", description = "API for managing authors")
public class AuthorController {

    private final AuthorService service;

    private AuthorController(AuthorService service) {
        this.service = service;
    }

    @Operation(summary = "Get all authors", description = "Retrieves a list of all authors, with optional pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of authors")
    })
    @GetMapping
    private ResponseEntity<Iterable<AuthorDTO>> getAuthors(
            @Parameter(description = "Page number for pagination (0-indexed).") @RequestParam(defaultValue = "0", required = false) Integer pageNumber,
            @Parameter(description = "Number of authors per page.") @RequestParam(defaultValue = "0", required = false) Integer pageSize,
            @Parameter(description = "Set to true to enable pagination.") @RequestParam(defaultValue = "false") Boolean paged) {

        log.debug("Fetch all Authors: [pageNumber: {}, pageSize: {}, paged: {}]", pageNumber, pageSize, paged);

        return ResponseEntity.ok((!paged)
                ? service.findAll()
                : service.findAll(pageNumber, pageSize));
    }

    @Operation(summary = "Get an author by ID", description = "Retrieves a single author by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the author"),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
    })
    @GetMapping("/{id}")
    private ResponseEntity<AuthorDTO> getAuthorById(@Parameter(description = "Unique ID of the author") @PathVariable UUID id) {
        log.debug("Fetch Author: [Id: {}]", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Find authors by first name", description = "Retrieves authors matching the given first name, with optional pagination and sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of authors")
    })
    @GetMapping("/firstName")
    private ResponseEntity<Iterable<AuthorDTO>> getAuthorsByFirstName(
            @Parameter(description = "First name to search for.", required = true) @RequestParam String firstName,
            @Parameter(description = "Page number for pagination (0-indexed).") @RequestParam(defaultValue = "0", required = false) Integer pageNumber,
            @Parameter(description = "Number of authors per page.") @RequestParam(defaultValue = "0", required = false) Integer pageSize,
            @Parameter(description = "Set to true to enable pagination.") @RequestParam(defaultValue = "false") Boolean paged,
            @Parameter(description = "Set to true to sort the results.") @RequestParam(defaultValue = "false") Boolean sorted,
            @Parameter(description = "Sort order (ASC or DESC).") @RequestParam(defaultValue = "ASC") SortOrder sortOrder) {

        log.debug("Fetch all Authors: [firstName: {}, pageNumber: {}, pageSize: {}, paged: {}, sorted: {}, sortOrder: {}]", firstName, pageNumber, pageSize, paged, sorted, sortOrder);

        return ResponseEntity.ok((!paged)
                ? service.findByFirstName(firstName)
                : service.findByFirstName(firstName, pageNumber, pageSize, sorted, sortOrder));
    }

    @Operation(summary = "Find authors by last name", description = "Retrieves authors matching the given last name, with optional pagination and sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of authors")
    })
    @GetMapping("/lastName")
    private ResponseEntity<Iterable<AuthorDTO>> getAuthorsByLastName(
            @Parameter(description = "Last name to search for.", required = true) @RequestParam String lastName,
            @Parameter(description = "Page number for pagination (0-indexed).") @RequestParam(defaultValue = "0", required = false) Integer pageNumber,
            @Parameter(description = "Number of authors per page.") @RequestParam(defaultValue = "0", required = false) Integer pageSize,
            @Parameter(description = "Set to true to enable pagination.") @RequestParam(defaultValue = "false") Boolean paged,
            @Parameter(description = "Set to true to sort the results.") @RequestParam(defaultValue = "false") Boolean sorted,
            @Parameter(description = "Sort order (ASC or DESC).") @RequestParam(defaultValue = "ASC") SortOrder sortOrder) {


        log.debug("Fetch all Authors: [lastName: {}, pageNumber: {}, pageSize: {}, paged: {}, sorted: {}, sortOrder: {}]", lastName, pageNumber, pageSize, paged, sorted, sortOrder);

        return ResponseEntity.ok((!paged)
                ? service.findByLastName(lastName)
                : service.findByLastName(lastName, pageNumber, pageSize, sorted, sortOrder));
    }

    @Operation(summary = "Find authors by first and last name", description = "Retrieves authors matching both the first and last name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of authors")
    })
    @GetMapping("/name")
    private ResponseEntity<Iterable<AuthorDTO>> getAuthorsByFirstNameAndLastName(
            @Parameter(description = "First name to search for.", required = true) @RequestParam String firstName,
            @Parameter(description = "Last name to search for.", required = true) @RequestParam String lastName) {

        log.debug("Fetch all authors: [firstName: {}, lastName: {}]", firstName, lastName);
        return ResponseEntity.ok(service.findByFirstNameAndLastName(firstName, lastName));
    }

    @Operation(summary = "Find authors by genre", description = "Retrieves authors who have written books in the specified genre, with optional pagination and sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of authors")
    })
    @GetMapping("/genre")
    private ResponseEntity<Iterable<AuthorDTO>> getAuthorsByGenre(
            @Parameter(description = "Genre to search for.", required = true) @RequestParam String genre,
            @Parameter(description = "Page number for pagination (0-indexed).") @RequestParam(defaultValue = "0", required = false) Integer pageNumber,
            @Parameter(description = "Number of authors per page.") @RequestParam(defaultValue = "0", required = false) Integer pageSize,
            @Parameter(description = "Set to true to enable pagination.") @RequestParam(defaultValue = "false") Boolean paged,
            @Parameter(description = "Set to true to sort the results.") @RequestParam(defaultValue = "false") Boolean sorted,
            @Parameter(description = "Sort order (ASC or DESC).") @RequestParam(defaultValue = "ASC") SortOrder sortOrder) {

        log.debug("Fetch all Authors - [genre: {}, pageNumber: {}, pageSize: {}, paged: {}, sorted: {}, sortOrder: {}]", genre, pageNumber, pageSize, paged, sorted, sortOrder);

        return ResponseEntity.ok((!paged)
                ? service.findByGenre(genre)
                : service.findByGenre(genre, pageNumber, pageSize, sorted, sortOrder));
    }

    @Operation(summary = "Create or update an author", description = "Creates a new author or updates an existing one if an ID is provided in the request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author created or updated successfully",
                    content = @Content(schema = @Schema(implementation = AuthorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid author data provided", content = @Content)
    })
    @RequestMapping(method = {RequestMethod.POST,RequestMethod.PUT})
    private ResponseEntity<?> saveAuthor(
            @Parameter(description = "Author object to be saved. For updates, include the author's ID.", required = true) @Valid @RequestBody AuthorDTO author) {

        log.debug("Save Author: [{}]", author.toString());

        AuthorDTO result = service.save(author);

        log.debug("Author Saved: [{}]", result.toString());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.status(HttpStatus.OK).location(location).body(result);
    }

    @Operation(summary = "Delete an author", description = "Deletes an author by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    private ResponseEntity<AuthorDTO> deleteAuthor(@Parameter(description = "Unique ID of the author to delete") @PathVariable UUID id) {
        log.debug("Delete Author: [{}]", id.toString());
        AuthorDTO deleted = service.delete(id);
        log.debug("Author Deleted: [{}]", deleted);

        return ResponseEntity.ok(deleted);
    }
}
