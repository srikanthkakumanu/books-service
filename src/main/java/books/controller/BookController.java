package books.controller;

import books.dto.BookDTO;
import books.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/books")
@RestController
@Tag(name = "Book API", description = "API for managing books")
public class BookController {


    private final BookService service;


    @Autowired
    private BookController(BookService bookService) {
        this.service = bookService;
    }

    @Operation(summary = "Ping endpoint", description = "A simple health check endpoint that returns 'Pong'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is responsive")
    })
    @GetMapping("/ping")
    private ResponseEntity<?> ping() {
        log.debug("Ping");
        return ResponseEntity.ok("Pong");
    }

    @Operation(summary = "Get all books", description = "Retrieves a list of all books in the library.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books")
    })
    @GetMapping
    private ResponseEntity<Iterable<BookDTO>> getBooks() {
        log.debug("Fetch all books");
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Get a book by ID", description = "Retrieves a single book by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the book"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    @GetMapping("/{id}")
    private ResponseEntity<BookDTO> getBookById(@Parameter(description = "Unique ID of the book") @PathVariable UUID id) {
        log.debug("Fetch Book By Id: [id: {}]", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Find books by title", description = "Retrieves a list of books matching the given title.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books")
    })
    @GetMapping("/title")
    private ResponseEntity<Iterable<BookDTO>> getBooksByTitle(
            @Parameter(description = "Title of the book to search for.", required = true) @RequestParam String title) {

        log.debug("Fetch all books: [title: {}]", title);
        return ResponseEntity.ok(service.findByTitle(title));
    }

    @Operation(summary = "Find books by ISBN", description = "Retrieves a list of books matching the given ISBN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books")
    })
    @GetMapping("/isbn")
    private ResponseEntity<Iterable<BookDTO>> getBooksByIsbn(
            @Parameter(description = "ISBN of the book to search for.", required = true) @RequestParam String isbn) {

        log.debug("Fetch all books: [isbn: {}]", isbn);
        return ResponseEntity.ok(service.findByIsbn(isbn));
    }

    @Operation(summary = "Find books by publisher", description = "Retrieves a list of books from the given publisher.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books")
    })
    @GetMapping("/publisher")
    private ResponseEntity<Iterable<BookDTO>> getBooksByPublisher(
            @Parameter(description = "Publisher of the book to search for.", required = true) @RequestParam String publisher) {

        log.debug("Fetch all books: [publisher: {}]", publisher);
        return ResponseEntity.ok(service.findByPublisher(publisher));
    }

    @Operation(summary = "Find books by author ID", description = "Retrieves a list of books written by a specific author.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books")
    })
    @GetMapping("/author")
    private ResponseEntity<Iterable<BookDTO>> getBooksByAuthorId(
            @Parameter(description = "Unique ID of the author.", required = true) @RequestParam UUID authorId) {

        log.debug("Fetch all books: [authorId: {}]", authorId);
        return ResponseEntity.ok(service.findByAuthorId(authorId));
    }

    @Operation(summary = "Find books by user ID", description = "This endpoint seems to be for a feature not fully described. It finds books by a user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books")
    })
    @GetMapping("/userId")
    private ResponseEntity<Iterable<BookDTO>> getBooksByUserId(
            @Parameter(description = "Unique ID of the user.", required = true) @RequestParam UUID userId) {

        log.debug("Fetch all books: [userId: {}]", userId);
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @Operation(summary = "Find books by user name", description = "This endpoint seems to be for a feature not fully described. It finds books by a user name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books")
    })
    @GetMapping("/userName")
    private ResponseEntity<Iterable<BookDTO>> getBooksByUserName(
            @Parameter(description = "Username of the user.", required = true) @RequestParam String userName) {

        log.debug("Fetch all books: [userName: {}]", userName);
        return ResponseEntity.ok(service.findByUserName(userName));
    }

    @Operation(summary = "Create or update a book", description = "Creates a new book or updates an existing one if an ID is provided in the request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book created or updated successfully",
                    content = @Content(schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid book data provided", content = @Content)
    })
    @RequestMapping(method = {RequestMethod.POST,RequestMethod.PUT})
    private ResponseEntity<BookDTO> saveBook(
            @Parameter(description = "Book object to be saved. For updates, include the book's ID.", required = true) @Valid @RequestBody BookDTO book) {

        log.debug("save: [{}]", book.toString());
        BookDTO result = service.save(book);

        log.debug("Book Saved : [{}]", result.toString());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.status(HttpStatus.OK).location(location).body(result);
    }

    @Operation(summary = "Delete a book", description = "Deletes a book by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    private ResponseEntity<BookDTO> deleteBook(@Parameter(description = "Unique ID of the book to delete") @PathVariable UUID id) {

        log.debug("Delete video: [Id: {}]", id);
        BookDTO deleted = service.delete(id);
        return ResponseEntity.ok(deleted);
    }
}
