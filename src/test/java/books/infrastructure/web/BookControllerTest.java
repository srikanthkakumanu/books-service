package books.infrastructure.web;

import books.infrastructure.config.SecurityConfig;
import books.infrastructure.web.facade.BookService;
import books.infrastructure.web.dto.BookDTO;
import books.infrastructure.web.dto.BookRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = BookController.class, properties = {
        "spring.cloud.vault.enabled=false", "spring.cloud.config.enabled=false", "spring.config.import="
})
@Import(SecurityConfig.class)
class BookControllerTest {
    @Autowired
    MockMvc mvc;
    @MockitoBean
    BookService service;
    @MockitoBean
    JwtDecoder decoder;

    @Test
    void create_withToken_shouldBindImmutableWriteRequest() throws Exception {
        when(service.save(any(BookRequest.class))).thenReturn(BookDTO.builder()
                .id(java.util.UUID.randomUUID()).title("Example").build());
        mvc.perform(post("/api/books").with(jwt()).contentType("application/json")
                        .content("{\"title\":\"Example\",\"completed\":false}"))
                .andExpect(status().isOk());
        verify(service).save(argThat(request -> request.title().equals("Example") && Boolean.FALSE.equals(request.completed())));
    }

    @Test
    void list_withoutToken_shouldReturnUnauthorized() throws Exception {
        mvc.perform(get("/api/books")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void titleSearch_withToken_shouldBindQueryParameter() throws Exception {
        when(service.findByTitle("Example")).thenReturn(List.of());
        mvc.perform(get("/api/books/title").param("title", "Example").with(jwt()))
                .andExpect(status().isOk());
        verify(service).findByTitle("Example");
    }

    @Test
    void ping_withoutToken_shouldReturnOk() throws Exception {
        mvc.perform(get("/api/books/ping")).andExpect(status().isOk());
        verifyNoInteractions(service);
    }
}
