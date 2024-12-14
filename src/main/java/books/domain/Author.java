package books.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tbl_authors")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Author extends BaseEntity {

    private String firstName;
    private String lastName;
    private String genre;
}
