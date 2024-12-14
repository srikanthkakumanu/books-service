package books.service;

import books.dto.BookDTO;
import java.util.UUID;

public interface BookService {
    public BookDTO save(BookDTO dto);
    public BookDTO delete(UUID id);
    public BookDTO findById(UUID id);
    public Iterable<BookDTO> findAll();
    public Iterable<BookDTO> findByTitle(String title);
    public Iterable<BookDTO> findByIsbn(String isbn);
    public Iterable<BookDTO> findByPublisher(String publisher);
    public Iterable<BookDTO> findByAuthorId(UUID authorId);
    public Iterable<BookDTO> findByUserId(UUID userId);
    public Iterable<BookDTO> findByUserName(String userName);
}
