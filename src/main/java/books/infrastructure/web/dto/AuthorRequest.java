package books.infrastructure.web.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AuthorRequest(UUID id, @Size(min = 1, max = 255) String firstName,
                            @Size(max = 255) String lastName, @Size(max = 255) String genre) {}
