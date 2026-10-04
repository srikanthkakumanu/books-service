package books.infrastructure.web.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record BookRequest(UUID id, @Size(min = 1, max = 100) String title,
                          @Size(max = 100) String description, String isbn, String publisher,
                          UUID authorId, Boolean completed, UUID userId, @Size(max = 20) String userName) {}
