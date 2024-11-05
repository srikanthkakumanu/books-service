package books.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tbl_books")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Book extends BaseEntity {

    @Valid
    @Size(min = 1, max = 100, message = "title must be between 1 and 100 characters")
    private String title;

    @Size(min = 1, max = 100, message = "description must be between 1 and 100 characters")
    private String description;

    private String isbn;
    private String publisher;

    @JdbcTypeCode(value = Types.VARBINARY)
    @Column(columnDefinition = "VARBINARY(16)", updatable = true, nullable = true)
    private UUID authorId;

    private Boolean completed;

    @JdbcTypeCode(value = Types.VARBINARY)
    @Column(columnDefinition = "VARBINARY(16)", updatable = true, nullable = true)
    private UUID userId;

    private String userName;

    public Book(String title, String isbn, String publisher, UUID authorId) {
        this.title = title;
        this.isbn = isbn;
        this.publisher = publisher;
        this.authorId = authorId;
    }

    public Book(String title, String isbn, String publisher, UUID authorId, Boolean completed, UUID userId, String userName) {
        this.title = title;
        this.isbn = isbn;
        this.publisher = publisher;
        this.authorId = authorId;
        this.completed = completed;
        this.userId = userId;
        this.userName = userName;
    }

    public Book(String title, String isbn, String publisher, UUID authorId, UUID userId, String userName) {
        this.title = title;
        this.isbn = isbn;
        this.publisher = publisher;
        this.authorId = authorId;
        this.userId = userId;
        this.userName = userName;
    }


}
