package books.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookDTO extends BaseDTO {

    @Valid
    @Size(min = 1, max = 100, message = "title must be between 1 and 100 characters")
    private String title;

    @Size(min = 1, max = 100, message = "description must be between 1 and 100 characters")
    private String description;

    private String isbn;
    private String publisher;
    private UUID authorId;
    private Boolean completed;
    private UUID userId;
    private String userName;

    public BookDTO(String title, String isbn, String publisher, UUID authorId) {
        this.title = title;
        this.isbn = isbn;
        this.publisher = publisher;
        this.authorId = authorId;
    }

    public BookDTO(String title, String isbn, String publisher, UUID authorId, Boolean completed, UUID userId, String userName) {
        this.title = title;
        this.isbn = isbn;
        this.publisher = publisher;
        this.authorId = authorId;
        this.completed = completed;
        this.userId = userId;
        this.userName = userName;
    }

    public BookDTO(String title, String isbn, String publisher, UUID authorId, UUID userId, String userName) {
        this.title = title;
        this.isbn = isbn;
        this.publisher = publisher;
        this.authorId = authorId;
        this.userId = userId;
        this.userName = userName;
    }
}
