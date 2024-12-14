package books.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import books.domain.Book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    Optional<List<Book>> findByTitle(String title);
    Optional<List<Book>> findByIsbn(String isbn);
    Optional<List<Book>> findByPublisher(String publisher);
    Optional<List<Book>> findByAuthorId(UUID authorId);
    Optional<List<Book>> findByUserId(UUID userId);
    Optional<List<Book>> findByUserName(String userName);
}
