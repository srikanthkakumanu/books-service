package books.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorDTO extends BaseDTO {

    @Valid
    @NotEmpty(message = "firstName must not be empty.")
    @Size(min = 1, max = 30, message = "firstName must be between 1 and 30 characters")
    private String firstName;

    @NotEmpty(message = "lastName must not be empty.")
    @Size(min = 1, max = 30, message = "lastName must be between 1 and 30 characters")
    private String lastName;

    @NotEmpty(message = "genre must not be empty.")
    private String genre;

}
