package books.infrastructure.persistence;

import books.domain.model.Author;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AuthorPersistenceMapper {
    Author toDomain(books.infrastructure.persistence.entity.Author entity);
    books.infrastructure.persistence.entity.Author toEntity(Author domain);
}
