package books.service;

import books.domain.Book;
import books.dto.BookDTO;
import books.exception.BooksServiceException;
import books.mapper.BookMapper;
import books.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository repository;
    private final BookMapper mapper;

    @Autowired
    public BookServiceImpl(BookRepository repository, BookMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public BookDTO save(BookDTO dto) {
        log.debug("save: [{}]", dto.toString());

        if (Objects.nonNull(dto.getId())) {
            Optional<Book> foundOptional = repository.findById(dto.getId());

            Optional<BookDTO> updated = foundOptional.map(found -> {
                if (Objects.nonNull(dto.getTitle()))
                    found.setTitle(dto.getTitle());
                if (Objects.nonNull(dto.getDescription()))
                    found.setDescription(dto.getDescription());
                if (Objects.nonNull(dto.getAuthorId()))
                    found.setAuthorId(dto.getAuthorId());
                if (Objects.nonNull(dto.getIsbn()))
                    found.setIsbn(dto.getIsbn());
                if (Objects.nonNull(dto.getPublisher()))
                    found.setPublisher(dto.getPublisher());
                if (Objects.nonNull(dto.getUserId()))
                    found.setUserId(dto.getUserId());
                if (Objects.nonNull(dto.getUserName()))
                    found.setUserName(dto.getUserName());
                if (Objects.nonNull(dto.getCompleted()))
                    found.setCompleted(dto.getCompleted());

                return mapper.toDTO(repository.save(found));
            });

            if (updated.isPresent())
                return updated.get();
        }

        Book saved = repository.save(mapper.toDomain(dto));
        log.info("Generated Id after saving Book: {}", saved.getId());
        return mapper.toDTO(saved);
    }

    @Override
    public BookDTO delete(UUID id) {
        log.debug("delete: [Id: {}]", id);

        Book found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book with id {} not found", id);
                    return new BooksServiceException("id", HttpStatus.NOT_FOUND, "Video does not exist");
                });

        repository.delete(found);
        return mapper.toDTO(found);
    }

    @Override
    public BookDTO findById(UUID id) {

        log.debug("findById: [Id: {}]", id);

        Book found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book with id {} not found", id);
                    return new BooksServiceException("id", HttpStatus.NOT_FOUND, "Book does not exist");
                });

        return mapper.toDTO(found);
    }

    @Override
    public Iterable<BookDTO> findAll() {
        log.debug("findAll() called");
        List<Book> books = repository.findAll();
        log.debug("Books: length: {}", books.size());

        return books.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public Iterable<BookDTO> findByTitle(String title) {

        log.debug("findByTitle: [title: {}]", title);

        return repository.findByTitle(title)
                .orElseThrow(() -> {
                    log.error("Book with title {} not found", title);
                    return new BooksServiceException("id", HttpStatus.NOT_FOUND, "Book does not exist");
                })
                .parallelStream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public Iterable<BookDTO> findByIsbn(String isbn) {

        log.debug("findByIsbn: [isbn: {}]", isbn);

        return repository.findByIsbn(isbn)
                .orElseThrow(() -> {
                    log.error("Book with isbn {} not found", isbn);
                    return new BooksServiceException("isbn", HttpStatus.NOT_FOUND, "Book does not exist");
                })
                .parallelStream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public Iterable<BookDTO> findByPublisher(String publisher) {
        log.debug("findByPublisher: [publisher: {}]", publisher);

        return repository.findByPublisher(publisher)
                .orElseThrow(() -> {
                    log.error("Book with publisher {} not found", publisher);
                    return new BooksServiceException("publisher", HttpStatus.NOT_FOUND, "Book does not exist");
                })
                .parallelStream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public Iterable<BookDTO> findByAuthorId(UUID authorId) {

        log.debug("findByAuthorId: [authorId: {}]", authorId);

        return repository.findByAuthorId(authorId)
                .orElseThrow(() -> {
                    log.error("Book with authorId {} not found", authorId);
                    return new BooksServiceException("authorId", HttpStatus.NOT_FOUND, "Book does not exist");
                })
                .parallelStream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<BookDTO> findByUserId(UUID userId) {
        log.debug("findByUserId: [userId: {}]", userId);

        return repository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Book with userId {} not found", userId);
                    return new BooksServiceException("userId", HttpStatus.NOT_FOUND, "Book does not exist");
                })
                .parallelStream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<BookDTO> findByUserName(String userName) {
        log.debug("findByUserName: [userName: {}]", userName);

        return repository.findByUserName(userName)
                .orElseThrow(() -> {
                    log.error("Book with userName {} not found", userName);
                    return new BooksServiceException("userName", HttpStatus.NOT_FOUND, "Book does not exist");
                })
                .parallelStream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}