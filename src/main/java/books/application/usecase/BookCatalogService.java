package books.application.usecase;

import books.domain.model.Book;
import books.domain.model.BookChanges;
import books.domain.model.BookNotFoundException;
import books.domain.model.CatalogActor;
import books.domain.port.in.BookCatalog;
import books.domain.port.out.BookStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BookCatalogService implements BookCatalog {
    private final BookStore store;

    public BookCatalogService(BookStore store) {
        this.store = store;
    }

    @Override
    @Transactional
    public Book save(UUID id, BookChanges changes, CatalogActor actor) {
        var book = id == null ? Book.create(changes, actor) : findById(id).revise(changes, actor);
        return store.save(book);
    }

    @Override
    @Transactional
    public Book delete(UUID id, CatalogActor actor) {
        var book = findById(id);
        book.requireWriteAccess(actor);
        store.delete(book);
        return book;
    }

    @Override
    public Book findById(UUID id) {
        return store.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @Override public List<Book> findAll() { return store.findAll(); }
    @Override public List<Book> findByTitle(String value) { return store.findByTitle(value); }
    @Override public List<Book> findByIsbn(String value) { return store.findByIsbn(value); }
    @Override public List<Book> findByPublisher(String value) { return store.findByPublisher(value); }
    @Override public List<Book> findByAuthorId(UUID value) { return store.findByAuthorId(value); }
    @Override public List<Book> findByUserId(UUID value) { return store.findByUserId(value); }
    @Override public List<Book> findByUserName(String value) { return store.findByUserName(value); }
}
