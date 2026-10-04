package books.infrastructure.web.facade;

import books.infrastructure.web.dto.AuthorDTO;
import books.infrastructure.web.dto.AuthorRequest;
import books.infrastructure.web.dto.SortOrder;

import java.util.Collection;
import java.util.UUID;

public interface AuthorService {

    public AuthorDTO save(AuthorRequest author);
    public Iterable<AuthorDTO> save(Collection<AuthorRequest> authors);
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
