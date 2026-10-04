package books.infrastructure.security;

import books.domain.model.CatalogActor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class CurrentCatalogActor {
    public CatalogActor get() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            throw new AccessDeniedException("A Keycloak bearer identity is required");
        }
        boolean manager = jwt.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals("ROLE_ADMIN") || authority.getAuthority().equals("ROLE_MANAGER"));
        try {
            return new CatalogActor(UUID.fromString(jwt.getToken().getSubject()), manager);
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("Keycloak subject must be a UUID", e);
        }
    }
}
