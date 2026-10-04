package books.domain.model;

import java.util.UUID;

public record BookChanges(String title, String description, String isbn, String publisher,
                          UUID authorId, Boolean completed, UUID userId, String userName) {}
