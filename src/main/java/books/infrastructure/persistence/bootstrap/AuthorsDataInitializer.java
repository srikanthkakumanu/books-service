package books.infrastructure.persistence.bootstrap;

import books.infrastructure.persistence.entity.Author;
import books.infrastructure.persistence.repository.AuthorRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Profile("seed")
@Order(1)
@Component
public class AuthorsDataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final ObjectMapper objectMapper;

    public AuthorsDataInitializer(AuthorRepository authorRepository, ObjectMapper objectMapper) {
        this.authorRepository = authorRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (authorRepository.count() > 0) { return; }
        log.debug("Loading Authors Data..");
        List<Author> authors = new CopyOnWriteArrayList<>();
        JsonNode json;

        try (InputStream inputStream = AuthorsDataInitializer.class.getResourceAsStream("/data/authors.json")) {
            json = objectMapper.readValue(inputStream, JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Authors JSON", e);
        }

        JsonNode edges = getEdges(json);
        for (JsonNode edge : edges) {
            authors.add(createAuthorFromNode(edge));
        }

        authorRepository.saveAll(authors);
        log.debug("Loaded Authors Data.");
    }

    private Author createAuthorFromNode(JsonNode edge) {
        String firstName = edge.get("first_name").asText();
        String lastName = edge.get("last_name").asText();
        String genre = edge.get("genre").asText();

        return new Author(firstName, lastName, genre);
    }

    private JsonNode getEdges(JsonNode json) {
        return Optional.ofNullable(json)
                .map(j -> j.get("authors"))
                .orElseThrow(() -> new IllegalArgumentException("Invalid JSON Object"));
    }
}
