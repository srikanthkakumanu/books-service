package books.service;

import books.dto.AuthorDTO;
import books.dto.SortOrder;

import java.util.Collection;
import java.util.UUID;

public interface AuthorService {

    public AuthorDTO save(AuthorDTO author);
    public Iterable<AuthorDTO> save(Collection<AuthorDTO> authors);
    public AuthorDTO delete(UUID id);

    public Iterable<AuthorDTO> findAll();
    public AuthorDTO findById(UUID id);
    public Iterable<AuthorDTO> findByFirstName(String firstName);
    public Iterable<AuthorDTO> findByLastName(String lastName);
    public Iterable<AuthorDTO> findByFirstNameAndLastName(String firstName, String lastName);
    public Iterable<AuthorDTO> findByGenre(String genre);

    public Iterable<AuthorDTO> findAll(int pageNum, int pageSize);
    Iterable<AuthorDTO> findByFirstNameAndLastName(String firstName,
                                           String lastName,
                                           int pageNum,
                                           int pageSize,
                                           Boolean sorted,
                                           SortOrder sortOrder);

    Iterable<AuthorDTO> findByFirstName(String firstName,
                                int pageNum,
                                int pageSize,
                                Boolean sorted,
                                SortOrder sortOrder);

    Iterable<AuthorDTO> findByLastName(String lastName,
                               int pageNum,
                               int pageSize,
                               Boolean sorted,
                               SortOrder sortOrder);

    Iterable<AuthorDTO> findByGenre(String genre,
                            int pageNum,
                            int pageSize,
                            Boolean sorted,
                            SortOrder sortOrder);
}
