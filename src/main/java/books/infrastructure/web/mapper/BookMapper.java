package books.infrastructure.web.mapper;

import books.domain.model.Book;
import books.domain.model.BookChanges;
import books.infrastructure.web.dto.BookDTO;
import books.infrastructure.web.dto.BookRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookMapper {
    BookDTO toDTO(Book domain);
    BookChanges toChanges(BookRequest dto);

    default java.time.LocalDateTime toLocalDateTime(java.time.Instant value) {
        return value == null ? null : java.time.LocalDateTime.ofInstant(value, java.time.ZoneOffset.UTC);
    }
}
