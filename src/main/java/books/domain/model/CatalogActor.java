package books.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CatalogActor(UUID userId, boolean managesCatalog) {
    public CatalogActor {
        Objects.requireNonNull(userId, "User identity is required");
    }
}
