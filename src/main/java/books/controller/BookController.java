package books.controller;

import books.dto.BookDTO;
import books.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/books")
@RestController
public class BookController {


    private final BookService service;


    @Autowired
    private BookController(BookService bookService) {
        this.service = bookService;
    }

    @GetMapping
    private ResponseEntity<Iterable<BookDTO>> getBooks() {
        log.debug("Fetch all books");
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    private ResponseEntity<BookDTO> getBookById(@PathVariable UUID id) {
        log.debug("Fetch Book By Id: [id: {}]", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/title")
    private ResponseEntity<Iterable<BookDTO>> getBooksByTitle(
            @PathVariable String title) {

        log.debug("Fetch all books: [title: {}]", title);
        return ResponseEntity.ok(service.findByTitle(title));
    }

    @GetMapping("/isbn")
    private ResponseEntity<Iterable<BookDTO>> getBooksByIsbn(
            @PathVariable String isbn) {

        log.debug("Fetch all books: [isbn: {}]", isbn);
        return ResponseEntity.ok(service.findByIsbn(isbn));
    }

    @GetMapping("/publisher")
    private ResponseEntity<Iterable<BookDTO>> getBooksByPublisher(
            @PathVariable String publisher) {

        log.debug("Fetch all books: [publisher: {}]", publisher);
        return ResponseEntity.ok(service.findByPublisher(publisher));
    }

    @GetMapping("/author")
    private ResponseEntity<Iterable<BookDTO>> getBooksByAuthorId(
            @PathVariable UUID authorId) {

        log.debug("Fetch all books: [authorId: {}]", authorId);
        return ResponseEntity.ok(service.findByAuthorId(authorId));
    }

    @GetMapping("/userId")
    private ResponseEntity<Iterable<BookDTO>> getBooksByUserId(
            @PathVariable UUID userId) {

        log.debug("Fetch all books: [userId: {}]", userId);
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @GetMapping("/userName")
    private ResponseEntity<Iterable<BookDTO>> getBooksByUserName(
            @PathVariable String userName) {

        log.debug("Fetch all books: [userName: {}]", userName);
        return ResponseEntity.ok(service.findByUserName(userName));
    }

    @RequestMapping(method = {RequestMethod.POST,RequestMethod.PUT})
    private ResponseEntity<BookDTO> saveBook(
            @Valid @RequestBody BookDTO book) {

        log.debug("save: [{}]", book.toString());
        BookDTO result = service.save(book);

        log.debug("Book Saved : [{}]", result.toString());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.status(HttpStatus.OK).location(location).body(result);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<BookDTO> deleteBook(@PathVariable UUID id) {

        log.debug("Delete video: [Id: {}]", id);
        BookDTO deleted = service.delete(id);
        return ResponseEntity.ok(deleted);
    }
}
