package books.service;

import books.domain.Author;
import books.dto.AuthorDTO;
import books.dto.SortOrder;
import books.exception.BooksServiceException;
import books.mapper.AuthorMapper;
import books.repository.AuthorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository repository;
    private final AuthorMapper mapper;

    public AuthorServiceImpl(AuthorRepository repository,
                             AuthorMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AuthorDTO save(AuthorDTO dto) {
        log.debug("save: [{}]", dto.toString());

        if (Objects.nonNull(dto.getId())) {
            Optional<Author> foundOptional = repository.findById(dto.getId());

            Optional<AuthorDTO> updated = foundOptional.map(found -> {
                if (Objects.nonNull(dto.getFirstName()))
                    found.setFirstName(dto.getFirstName());
                if (Objects.nonNull(dto.getLastName()))
                    found.setLastName(dto.getLastName());
                if (Objects.nonNull(dto.getGenre()))
                    found.setGenre(dto.getGenre());
                return mapper.toDTO(repository.save(found));
            });

            if (updated.isPresent())
                return updated.get();
        }

        Author saved = repository.save(mapper.toDomain(dto));
        log.info("Generated Id after saving Author: {}", saved.getId());
        return mapper.toDTO(saved);
    }

    @Override
    public Iterable<AuthorDTO> save(Collection<AuthorDTO> authors) {
        log.debug("Save Authors");

        return authors.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public AuthorDTO delete(UUID id) {
        log.debug("delete: [Id: {}]", id);

        Author found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Author with id {} not found", id);
                    return new BooksServiceException("id", HttpStatus.NOT_FOUND, "Video does not exist");
                });

        repository.delete(found);
        return mapper.toDTO(found);
    }

    @Override
    public Iterable<AuthorDTO> findAll() {
        log.debug("findAll() called");
        List<Author> authors = repository.findAll();
        log.debug("Authors: length: {}", authors.size());

        return authors.parallelStream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public AuthorDTO findById(UUID id) {

        log.debug("findById: [Id: {}]", id);

        Author found = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Author with id {} not found", id);
                    return new BooksServiceException("id", HttpStatus.NOT_FOUND, "Author does not exist");
                });

        return mapper.toDTO(found);
    }

    @Override
    public Iterable<AuthorDTO> findByFirstNameAndLastName(String firstName, String lastName) {
        log.debug("Get all authors: [firstName: {} and lastName: {}]", firstName, lastName);

        List<Author> found =
                repository.findByFirstNameAndLastName(firstName, lastName)
                        .orElseThrow(() ->
                                new BooksServiceException("firstNameAndLastName", HttpStatus.NOT_FOUND,
                                        "Author[firstName: %s, lastName: %s] Not Found. "
                                                .formatted(firstName, lastName)));

        return found.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Iterable<AuthorDTO> findByFirstName(String firstName) {
        log.debug("Get all authors: [firstName: {}]", firstName);

        List<Author> found =
                repository.findByFirstName(firstName)
                        .orElseThrow(() ->
                                new BooksServiceException("firstName", HttpStatus.NOT_FOUND,
                                        "Author[firstName: %s] Not Found. "
                                                .formatted(firstName)));

        return found.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Iterable<AuthorDTO> findByLastName(String lastName) {

        log.debug("Get all authors: [lastName: {}]", lastName);

        List<Author> found =
                repository.findByLastName(lastName)
                        .orElseThrow(() ->
                                new BooksServiceException("lastName", HttpStatus.NOT_FOUND,
                                        "Author[lastName: %s] Not Found. "
                                                .formatted(lastName)));

        return found.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Iterable<AuthorDTO> findByGenre(String genre) {
        log.debug("Get all authors: [genre: {}]", genre);

        List<Author> found =
                repository.findByGenre(genre)
                        .orElseThrow(() ->
                                new BooksServiceException("genre", HttpStatus.NOT_FOUND,
                                        "Author[genre: %s] Not Found. "
                                                .formatted(genre)));

        return found.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Iterable<AuthorDTO> findAll(int pageNum, int pageSize) {

        log.debug("Get all authors: [pageNum: {}, pageSize: {}]", pageNum, pageSize);

        List<Author> allAuthors = repository.findAll(PageRequest.of(pageNum, pageSize)).getContent();

        return allAuthors.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Iterable<AuthorDTO> findByFirstNameAndLastName(String firstName,
                                                          String lastName,
                                                          int pageNum,
                                                          int pageSize,
                                                          Boolean sorted,
                                                          SortOrder sortOrder) {

        log.debug("Get all authors: [" +
                        "firstName: {}, lastName: {}, " +
                        "pageNum: {}, pageSize: {}, sorted: {}, sortOrder: {}]",
                firstName, lastName, pageNum, pageSize, sorted, sortOrder);

        List<Author> found;
        if (!sorted)
            found = repository.findByFirstNameAndLastName(
                    firstName,
                    lastName,
                    PageRequest.of(pageNum, pageSize))
                    .getContent();
        else {
            if (sortOrder == SortOrder.ASC)
                found = repository.findByFirstNameAndLastName(
                        firstName,
                        lastName,
                        PageRequest.of(
                                pageNum,
                                pageSize,
                                Sort.by(Sort.Order.asc("firstName"),
                                        Sort.Order.asc("lastName"))))
                        .getContent();
            else
                found = repository.findByFirstNameAndLastName(
                        firstName,
                        lastName,
                        PageRequest.of(
                                pageNum,
                                pageSize,
                                Sort.by(Sort.Order.desc("firstName"),
                                        Sort.Order.desc("lastName"))))
                        .getContent();
        }

        if (found.isEmpty())
            throw new BooksServiceException("firstNameAndLastName", HttpStatus.NOT_FOUND,
                    "Authors: [firstName: %s, lastName: %s] Not Found.".formatted(firstName, lastName));

        return found.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Iterable<AuthorDTO> findByFirstName(String firstName,
                                               int pageNum,
                                               int pageSize,
                                               Boolean sorted,
                                               SortOrder sortOrder) {

        log.debug("Get all authors: [" +
                        "firstName: {}, " +
                        "pageNum: {}, pageSize: {}, sorted: {}, sortOrder: {}]",
                firstName, pageNum, pageSize, sorted, sortOrder);

        List<Author> found;
        if (!sorted)
            found = repository.findByFirstName(firstName, PageRequest.of(pageNum, pageSize)).getContent();
        else {
            if (sortOrder == SortOrder.ASC)
                found = repository.findByFirstName(firstName, PageRequest.of(pageNum, pageSize, Sort.by(Sort.Order.asc("firstName")))).getContent();
            else
                found = repository.findByFirstName(firstName, PageRequest.of(pageNum, pageSize, Sort.by(Sort.Order.desc("firstName")))).getContent();
        }

        if (found.isEmpty())
            throw new BooksServiceException("firstName", HttpStatus.NOT_FOUND,
                    "Authors: [firstName: %s] Not Found.".formatted(firstName));

        return found.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Iterable<AuthorDTO> findByLastName(String lastName,
                                              int pageNum,
                                              int pageSize,
                                              Boolean sorted,
                                              SortOrder sortOrder) {

        log.debug("Get all authors: [" +
                        "lastName: {}, " +
                        "pageNum: {}, pageSize: {}, sorted: {}, sortOrder: {}]",
                lastName, pageNum, pageSize, sorted, sortOrder);

        List<Author> found;
        if (!sorted)
            found = repository.findByLastName(lastName, PageRequest.of(pageNum, pageSize)).getContent();
        else {
            if (sortOrder == SortOrder.ASC)
                found = repository.findByLastName(lastName, PageRequest.of(pageNum, pageSize, Sort.by(Sort.Order.asc("lastName")))).getContent();
            else
                found = repository.findByLastName(lastName, PageRequest.of(pageNum, pageSize, Sort.by(Sort.Order.desc("lastName")))).getContent();
        }

        if (found.isEmpty())
            throw new BooksServiceException("lastName", HttpStatus.NOT_FOUND,
                    "Authors: [lastName: %s] Not Found.".formatted(lastName));

        return found.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Iterable<AuthorDTO> findByGenre(String genre,
                                           int pageNum,
                                           int pageSize,
                                           Boolean sorted,
                                           SortOrder sortOrder) {

        log.debug("Get all authors: [" +
                        "genre: {}, " +
                        "pageNum: {}, pageSize: {}, sorted: {}, sortOrder: {}]",
                genre, pageNum, pageSize, sorted, sortOrder);

        List<Author> found;
        if (!sorted)
            found = repository.findByGenre(genre, PageRequest.of(pageNum, pageSize)).getContent();
        else {
            if (sortOrder == SortOrder.ASC)
                found = repository.findByGenre(genre, PageRequest.of(pageNum, pageSize, Sort.by(Sort.Order.asc("genre")))).getContent();
            else
                found = repository.findByGenre(genre, PageRequest.of(pageNum, pageSize, Sort.by(Sort.Order.desc("genre")))).getContent();
        }

        if (found.isEmpty())
            throw new BooksServiceException("genre", HttpStatus.NOT_FOUND,
                    "Authors: [genre: %s] Not Found.".formatted(genre));

        return found.stream().map(mapper::toDTO).collect(Collectors.toList());
    }
}
