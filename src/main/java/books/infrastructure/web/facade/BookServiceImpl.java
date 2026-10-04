package books.infrastructure.web.facade;

import books.domain.port.in.BookCatalog;
import books.infrastructure.web.dto.BookDTO;
import books.infrastructure.web.dto.BookRequest;
import books.infrastructure.web.mapper.BookMapper;
import books.infrastructure.security.CurrentCatalogActor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {
    private final BookCatalog catalog;
    private final BookMapper mapper;
    private final CurrentCatalogActor actor;

    public BookServiceImpl(BookCatalog catalog, BookMapper mapper, CurrentCatalogActor actor) {
        this.catalog = catalog;
        this.mapper = mapper;
        this.actor = actor;
    }

    @Override public BookDTO save(BookRequest dto) {
        return mapper.toDTO(catalog.save(dto.id(), mapper.toChanges(dto), actor.get()));
    }
    @Override public BookDTO delete(UUID id) { return mapper.toDTO(catalog.delete(id, actor.get())); }
    @Override public BookDTO findById(UUID id) { return mapper.toDTO(catalog.findById(id)); }
    @Override public Iterable<BookDTO> findAll() { return catalog.findAll().stream().map(mapper::toDTO).toList(); }
    @Override public Iterable<BookDTO> findByTitle(String value) { return catalog.findByTitle(value).stream().map(mapper::toDTO).toList(); }
    @Override public Iterable<BookDTO> findByIsbn(String value) { return catalog.findByIsbn(value).stream().map(mapper::toDTO).toList(); }
    @Override public Iterable<BookDTO> findByPublisher(String value) { return catalog.findByPublisher(value).stream().map(mapper::toDTO).toList(); }
    @Override public Iterable<BookDTO> findByAuthorId(UUID value) { return catalog.findByAuthorId(value).stream().map(mapper::toDTO).toList(); }
    @Override public Iterable<BookDTO> findByUserId(UUID value) { return catalog.findByUserId(value).stream().map(mapper::toDTO).toList(); }
    @Override public Iterable<BookDTO> findByUserName(String value) { return catalog.findByUserName(value).stream().map(mapper::toDTO).toList(); }
}
