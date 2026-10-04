package books.infrastructure.persistence;

import books.domain.model.Book;
import books.domain.port.out.BookStore;
import books.infrastructure.persistence.repository.BookRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaBookStore implements BookStore {
    private final BookRepository repository;
    private final BookPersistenceMapper mapper;

    public JpaBookStore(BookRepository repository, BookPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override public Book save(Book book) { return mapper.toDomain(repository.save(mapper.toEntity(book))); }
    @Override public Optional<Book> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    @Override public void delete(Book book) { repository.deleteById(book.id()); }
    @Override public List<Book> findAll() { return map(repository.findAll()); }
    @Override public List<Book> findByTitle(String value) { return map(repository.findByTitle(value).orElseGet(List::of)); }
    @Override public List<Book> findByIsbn(String value) { return map(repository.findByIsbn(value).orElseGet(List::of)); }
    @Override public List<Book> findByPublisher(String value) { return map(repository.findByPublisher(value).orElseGet(List::of)); }
    @Override public List<Book> findByAuthorId(UUID value) { return map(repository.findByAuthorId(value).orElseGet(List::of)); }
    @Override public List<Book> findByUserId(UUID value) { return map(repository.findByUserId(value).orElseGet(List::of)); }
    @Override public List<Book> findByUserName(String value) { return map(repository.findByUserName(value).orElseGet(List::of)); }

    private List<Book> map(List<books.infrastructure.persistence.entity.Book> entities) {
        return entities.stream().map(mapper::toDomain).toList();
    }
}
