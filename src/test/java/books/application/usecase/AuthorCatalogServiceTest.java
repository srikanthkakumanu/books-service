package books.application.usecase;

import books.domain.model.*;
import books.domain.port.out.AuthorStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthorCatalogServiceTest {
    @Mock AuthorStore store;
    @InjectMocks AuthorCatalogService catalog;

    @Test
    void save_byRegularUser_shouldDenyBeforePersistence() {
        assertThatThrownBy(() -> catalog.save(null, new AuthorChanges("Ada", "Lovelace", "Science"),
                new CatalogActor(UUID.randomUUID(), false))).isInstanceOf(CatalogAccessDeniedException.class);
        verifyNoInteractions(store);
    }

    @Test
    void query_invalidPageSize_shouldReject() {
        assertThatThrownBy(() -> new AuthorQuery(null, null, null, 0, 0, false, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
