package books.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Book(UUID id, Instant created, Instant updated,
                   String title, String description, String isbn, String publisher,
                   UUID authorId, Boolean completed, UUID userId, String userName) {
    public Book {
        if (title == null || title.isBlank() || title.length() > 100) {
            throw new IllegalArgumentException("Book title must contain 1 to 100 characters");
        }
        if (description != null && description.length() > 100) {
            throw new IllegalArgumentException("Book description must not exceed 100 characters");
        }
        title = title.strip();
        completed = Boolean.TRUE.equals(completed);
    }

    public Book revise(BookChanges changes, CatalogActor actor) {
        requireWriteAccess(actor);
        UUID owner = changes.userId() == null ? userId : changes.userId();
        if (!actor.managesCatalog() && !Objects.equals(owner, userId)) {
            throw new CatalogAccessDeniedException("Only a catalog manager may transfer book ownership");
        }
        return new Book(id, created, updated,
                changes.title() == null ? title : changes.title(),
                changes.description() == null ? description : changes.description(),
                changes.isbn() == null ? isbn : changes.isbn(),
                changes.publisher() == null ? publisher : changes.publisher(),
                changes.authorId() == null ? authorId : changes.authorId(),
                changes.completed() == null ? completed : changes.completed(), owner,
                changes.userName() == null ? userName : changes.userName());
    }

    public static Book create(BookChanges changes, CatalogActor actor) {
        UUID owner = actor.managesCatalog() && changes.userId() != null ? changes.userId() : actor.userId();
        return new Book(null, null, null, changes.title(), changes.description(), changes.isbn(),
                changes.publisher(), changes.authorId(), changes.completed(), owner, changes.userName());
    }

    public void requireWriteAccess(CatalogActor actor) {
        if (!actor.managesCatalog() && !Objects.equals(userId, actor.userId())) {
            throw new CatalogAccessDeniedException("Book may only be changed by its owner or a catalog manager");
        }
    }
}
