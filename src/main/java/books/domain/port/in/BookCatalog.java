package books.domain.port.in;

import books.domain.model.Book;
import books.domain.model.BookChanges;
import books.domain.model.CatalogActor;
import java.util.List;
import java.util.UUID;

public interface BookCatalog {
    Book save(UUID id, BookChanges changes, CatalogActor actor);
    Book delete(UUID id, CatalogActor actor);
    Book findById(UUID id);
    List<Book> findAll();
    List<Book> findByTitle(String title);
    List<Book> findByIsbn(String isbn);
    List<Book> findByPublisher(String publisher);
    List<Book> findByAuthorId(UUID authorId);
    List<Book> findByUserId(UUID userId);
    List<Book> findByUserName(String userName);
}
