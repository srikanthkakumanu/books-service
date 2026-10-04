package books.infrastructure.web.mapper;

import books.domain.model.Author;
import books.domain.model.AuthorChanges;
import books.infrastructure.web.dto.AuthorDTO;
import books.infrastructure.web.dto.AuthorRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthorMapper {
    AuthorDTO toDTO(Author domain);
    AuthorChanges toChanges(AuthorRequest dto);

    default java.time.LocalDateTime toLocalDateTime(java.time.Instant value) {
        return value == null ? null : java.time.LocalDateTime.ofInstant(value, java.time.ZoneOffset.UTC);
    }
}
