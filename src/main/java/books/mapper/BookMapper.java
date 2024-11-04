package books.mapper;

import books.domain.Book;
import books.dto.BookDTO;
import org.mapstruct.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BookMapper {
    public BookDTO toDTO(Book domain);
    public Book toDomain(BookDTO dto);

    @Mapping(target = "created", source = "created", qualifiedByName = "localDateTimeToTimestamp")
    @Mapping(target = "updated", source = "updated", qualifiedByName = "localDateTimeToTimestamp")
    public Book merge(BookDTO from, @MappingTarget Book to);

    @Named("localDateTimeToTimestamp")
    default Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }
}
