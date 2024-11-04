package books.mapper;

import books.domain.Author;
import books.dto.AuthorDTO;
import org.mapstruct.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AuthorMapper {
    public AuthorDTO toDTO(Author domain);
    public Author toDomain(AuthorDTO dto);

    @Mapping(target = "created", source = "created", qualifiedByName = "localDateTimeToTimestamp")
    @Mapping(target = "updated", source = "updated", qualifiedByName = "localDateTimeToTimestamp")
    public Author merge(AuthorDTO from, @MappingTarget Author to);

    @Named("localDateTimeToTimestamp")
    default Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }
}
