package books.infrastructure.persistence;

import books.domain.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface BookPersistenceMapper {
    Book toDomain(books.infrastructure.persistence.entity.Book entity);
    books.infrastructure.persistence.entity.Book toEntity(Book domain);
}
