package com.bookstore;

import com.bookstore.dto.AuthRequest;
import com.bookstore.dto.AuthorDto;
import com.bookstore.dto.BookRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class BookControllerIntegrationTest {

    @Autowired private WebApplicationContext context;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        AuthRequest req = new AuthRequest(username, password);
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("token").asText();
    }

    @Test
    void unauthenticated_request_returns_401() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void user_can_search_books() throws Exception {
        String token = loginAndGetToken("user", "user123");
        mockMvc.perform(get("/api/v1/books").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void user_can_add_book() throws Exception {
        String token = loginAndGetToken("user", "user123");
        BookRequest req = BookRequest.builder()
                .isbn("9780553103540")
                .title("A Game of Thrones")
                .authors(Set.of(new AuthorDto("George R.R. Martin", LocalDate.of(1948, 9, 20))))
                .year(1996)
                .price(25.0)
                .genre("Fantasy")
                .build();

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("9780553103540"))
                .andExpect(jsonPath("$.title").value("A Game of Thrones"));
    }

    @Test
    void duplicate_isbn_returns_409() throws Exception {
        String token = loginAndGetToken("user", "user123");
        BookRequest req = BookRequest.builder()
                .isbn("9780451524935")  
                .title("Duplicate")
                .authors(Set.of(new AuthorDto("Someone", LocalDate.of(1900, 1, 1))))
                .year(2020)
                .price(10.0)
                .genre("Fiction")
                .build();

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void invalid_book_returns_400() throws Exception {
        String token = loginAndGetToken("user", "user123");
        BookRequest req = BookRequest.builder()
                .isbn("not-a-valid-isbn")
                .title("")
                .authors(Set.of())
                .year(1000)
                .price(-5.0)
                .build();

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void user_cannot_delete_book_returns_403() throws Exception {
        String token = loginAndGetToken("user", "user123");
        mockMvc.perform(delete("/api/v1/books/9780451524935")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_delete_book() throws Exception {
        String token = loginAndGetToken("admin", "admin123");
        mockMvc.perform(delete("/api/v1/books/9781501142970")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonexistent_book_returns_404() throws Exception {
        String token = loginAndGetToken("admin", "admin123");
        mockMvc.perform(delete("/api/v1/books/9999999999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_by_title_exact_match() throws Exception {
        String token = loginAndGetToken("user", "user123");
        mockMvc.perform(get("/api/v1/books").param("title", "1984")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("9780451524935"));
    }

    @Test
    void search_by_author_exact_match() throws Exception {
        String token = loginAndGetToken("user", "user123");
        mockMvc.perform(get("/api/v1/books").param("author", "George Orwell")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("1984"));
    }

    @Test
    void invalid_login_returns_401() throws Exception {
        AuthRequest req = new AuthRequest("admin", "wrong-password");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
