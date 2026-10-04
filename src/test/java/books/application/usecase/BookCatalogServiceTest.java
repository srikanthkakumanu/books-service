package books.application.usecase;

import books.domain.model.*;
import books.domain.port.out.BookStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCatalogServiceTest {
    @Mock BookStore store;
    @InjectMocks BookCatalogService catalog;

    @Test
    void create_withSpoofedOwner_shouldUseAuthenticatedIdentity() {
        var owner = UUID.randomUUID();
        var changes = new BookChanges("Example", null, null, null, null, false, UUID.randomUUID(), null);
        when(store.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var saved = catalog.save(null, changes, new CatalogActor(owner, false));
        assertThat(saved.userId()).isEqualTo(owner);
    }

    @Test
    void delete_byAnotherUser_shouldDenyWithoutDeleting() {
        var book = book(UUID.randomUUID());
        when(store.findById(book.id())).thenReturn(Optional.of(book));
        assertThatThrownBy(() -> catalog.delete(book.id(), new CatalogActor(UUID.randomUUID(), false)))
                .isInstanceOf(CatalogAccessDeniedException.class);
        verify(store, never()).delete(any());
    }

    @Test
    void update_missingBook_shouldNotCreateReplacement() {
        var id = UUID.randomUUID();
        when(store.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> catalog.save(id, new BookChanges("Example", null, null, null, null, null, null, null),
                new CatalogActor(UUID.randomUUID(), true))).isInstanceOf(BookNotFoundException.class);
        verify(store, never()).save(any());
    }

    @Test
    void revise_byOwner_shouldKeepFieldsOmittedFromPatch() {
        var book = book(UUID.randomUUID());
        var updated = book.revise(new BookChanges(null, null, null, null, null, true, null, null),
                new CatalogActor(book.userId(), false));
        assertThat(updated.title()).isEqualTo(book.title());
        assertThat(updated.completed()).isTrue();
        assertThat(book.completed()).isFalse();
    }

    @Test
    void revise_ownerTransferByUser_shouldDeny() {
        var book = book(UUID.randomUUID());
        assertThatThrownBy(() -> book.revise(new BookChanges(null, null, null, null, null, null, UUID.randomUUID(), null),
                new CatalogActor(book.userId(), false))).isInstanceOf(CatalogAccessDeniedException.class);
    }

    @Test
    void create_blankTitle_shouldReject() {
        assertThatThrownBy(() -> Book.create(new BookChanges(" ", null, null, null, null, null, null, null),
                new CatalogActor(UUID.randomUUID(), false))).isInstanceOf(IllegalArgumentException.class);
    }

    private Book book(UUID owner) {
        return new Book(UUID.randomUUID(), null, null, "Example", null, null, null, null, false, owner, null);
    }
}
