package books.bootstrap;

import books.domain.*;
import books.repository.AuthorRepository;
import books.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.TypeReference;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Profile({"default", "dev"})
@Component
@Order(2)
public class BooksDataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final ObjectMapper objectMapper;

    public BooksDataInitializer(BookRepository bookRepository,
                                AuthorRepository authorRepository, ObjectMapper objectMapper) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        log.debug("Loading Books Data..");
        List<Author> allAuthors = new CopyOnWriteArrayList<>();
        List<Book> allBooks = new CopyOnWriteArrayList<>();
        JsonNode json;

        try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/books.json")) {
            json = objectMapper.readValue(inputStream, JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Books JSON", e);
        }

        allAuthors = authorRepository.findAll();
        int numOfAuthors = allAuthors.size();
        int counter = 0;

        JsonNode edges = getEdges(json);
        for (JsonNode edge : edges) {
            counter += 1;
            log.debug("counter: [{}]", counter);
            int idx = ThreadLocalRandom.current().nextInt(counter, numOfAuthors);
            log.debug("idx: [{}]", idx);
            UUID authorId = allAuthors.get(idx).getId();
            allBooks.add(createBookFromNode(edge, authorId));
        }

        bookRepository.saveAll(allBooks);
        log.debug("Loaded Books Data.");
    }


    private Book createBookFromNode(JsonNode edge, UUID authorId) {
        String title = edge.get("title").asText();
        String publisher = edge.get("publisher").asText();
        String isbn = edge.get("ISBN").asText();

        return new Book(title, isbn, publisher, authorId);
    }

    private JsonNode getEdges(JsonNode json) {
        return Optional.ofNullable(json)
                .map(j -> j.get("books"))
                .orElseThrow(() -> new IllegalArgumentException("Invalid JSON Object"));
    }
}
