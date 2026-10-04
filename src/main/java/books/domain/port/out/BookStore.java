package books.domain.port.out;

import books.domain.model.Book;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookStore {
    Book save(Book book);
    Optional<Book> findById(UUID id);
    void delete(Book book);
    List<Book> findAll();
    List<Book> findByTitle(String title);
    List<Book> findByIsbn(String isbn);
    List<Book> findByPublisher(String publisher);
    List<Book> findByAuthorId(UUID authorId);
    List<Book> findByUserId(UUID userId);
    List<Book> findByUserName(String userName);
}
