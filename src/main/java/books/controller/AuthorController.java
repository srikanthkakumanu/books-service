package books.controller;

import books.dto.AuthorDTO;
import books.dto.SortOrder;
import books.service.AuthorService;
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
public class AuthorController {

    private final AuthorService service;

    private AuthorController(AuthorService service) {
        this.service = service;
    }

    @GetMapping
    private ResponseEntity<Iterable<AuthorDTO>> getAuthors(
            @RequestParam(defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(defaultValue = "0", required = false) Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean paged) {

        log.debug("Fetch all Authors: [pageNumber: {}, pageSize: {}, paged: {}]", pageNumber, pageSize, paged);

        return ResponseEntity.ok((!paged)
                ? service.findAll()
                : service.findAll(pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    private ResponseEntity<AuthorDTO> getAuthorById(@PathVariable UUID id) {
        log.debug("Fetch Author: [Id: {}]", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/firstName")
    private ResponseEntity<Iterable<AuthorDTO>> getAuthorsByFirstName(
            @RequestParam String firstName,
            @RequestParam(defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(defaultValue = "0", required = false) Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean paged,
            @RequestParam(defaultValue = "false") Boolean sorted,
            @RequestParam(defaultValue = "ASC") SortOrder sortOrder) {

        log.debug("Fetch all Authors: [firstName: {}, pageNumber: {}, pageSize: {}, paged: {}, sorted: {}, sortOrder: {}]", firstName, pageNumber, pageSize, paged, sorted, sortOrder);

        return ResponseEntity.ok((!paged)
                ? service.findByFirstName(firstName)
                : service.findByFirstName(firstName, pageNumber, pageSize, sorted, sortOrder));
    }

    @GetMapping("/lastName")
    private ResponseEntity<Iterable<AuthorDTO>> getAuthorsByLastName(
            @RequestParam
            String lastName,
            @RequestParam(defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(defaultValue = "0", required = false) Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean paged,
            @RequestParam(defaultValue = "false") Boolean sorted,
            @RequestParam(defaultValue = "ASC") SortOrder sortOrder) {


        log.debug("Fetch all Authors: [lastName: {}, pageNumber: {}, pageSize: {}, paged: {}, sorted: {}, sortOrder: {}]", lastName, pageNumber, pageSize, paged, sorted, sortOrder);

        return ResponseEntity.ok((!paged)
                ? service.findByLastName(lastName)
                : service.findByLastName(lastName, pageNumber, pageSize, sorted, sortOrder));
    }

    @GetMapping("/name")
    private ResponseEntity<Iterable<AuthorDTO>> getAuthorsByFirstNameAndLastName(
            @RequestParam String firstName, @RequestParam String lastName) {

        log.debug("Fetch all authors: [firstName: {}, lastName: {}]", firstName, lastName);
        return ResponseEntity.ok(service.findByFirstNameAndLastName(firstName, lastName));
    }

    @GetMapping("/genre")
    private ResponseEntity<Iterable<AuthorDTO>> getAuthorsByGenre(
            @RequestParam String genre,
            @RequestParam(defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(defaultValue = "0", required = false) Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean paged,
            @RequestParam(defaultValue = "false") Boolean sorted,
            @RequestParam(defaultValue = "ASC") SortOrder sortOrder) {

        log.debug("Fetch all Authors - [genre: {}, pageNumber: {}, pageSize: {}, paged: {}, sorted: {}, sortOrder: {}]", genre, pageNumber, pageSize, paged, sorted, sortOrder);

        return ResponseEntity.ok((!paged)
                ? service.findByGenre(genre)
                : service.findByGenre(genre, pageNumber, pageSize, sorted, sortOrder));
    }

    @RequestMapping(method = {RequestMethod.POST,RequestMethod.PUT})
    private ResponseEntity<?> saveAuthor(
            @Valid @RequestBody AuthorDTO author) {

        log.debug("Save Author: [{}]", author.toString());

        AuthorDTO result = service.save(author);

        log.debug("Author Saved: [{}]", result.toString());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.status(HttpStatus.OK).location(location).body(result);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<AuthorDTO> deleteAuthor(@PathVariable UUID id) {
        log.debug("Delete Author: [{}]", id.toString());
        AuthorDTO deleted = service.delete(id);
        log.debug("Author Deleted: [{}]", deleted);

        return ResponseEntity.ok(deleted);
    }
}
