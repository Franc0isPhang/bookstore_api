package com.bookstore.service;

import com.bookstore.dto.AuthorDto;
import com.bookstore.dto.BookRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.DuplicateBookException;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Transactional
    public BookResponse addBook(BookRequest request) {
        if (bookRepository.existsById(request.getIsbn())) {
            throw new DuplicateBookException(request.getIsbn());
        }
        Book book = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .year(request.getYear())
                .price(request.getPrice())
                .genre(request.getGenre())
                .authors(resolveAuthors(request.getAuthors()))
                .build();

        Book saved = bookRepository.save(book);
        log.info("Added book ISBN={}", saved.getIsbn());
        return toResponse(saved);
    }

    @Transactional
    public BookResponse updateBook(String isbn, BookRequest request) {
        Book existing = bookRepository.findById(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));

        if (!isbn.equals(request.getIsbn())) {
            throw new IllegalArgumentException(
                    "ISBN in path (" + isbn + ") does not match body (" + request.getIsbn() + ")");
        }

        existing.setTitle(request.getTitle());
        existing.setYear(request.getYear());
        existing.setPrice(request.getPrice());
        existing.setGenre(request.getGenre());
        existing.setAuthors(resolveAuthors(request.getAuthors()));

        Book updated = bookRepository.save(existing);
        log.info("Updated book ISBN={}", updated.getIsbn());
        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> search(String title, String authorName) {
        String t = (title == null || title.isBlank()) ? null : title;
        String a = (authorName == null || authorName.isBlank()) ? null : authorName;
        return bookRepository.search(t, a).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteBook(String isbn) {
        if (!bookRepository.existsById(isbn)) {
            throw new BookNotFoundException(isbn);
        }
        bookRepository.deleteById(isbn);
        log.info("Deleted book ISBN={}", isbn);
    }

    private Set<Author> resolveAuthors(Set<AuthorDto> dtos) {
        return dtos.stream()
                .map(dto -> authorRepository.findByName(dto.getName())
                        .orElseGet(() -> authorRepository.save(Author.builder()
                                .name(dto.getName())
                                .birthday(dto.getBirthday())
                                .build())))
                .collect(Collectors.toSet());
    }

    private BookResponse toResponse(Book book) {
        Set<AuthorDto> authorDtos = book.getAuthors().stream()
                .map(a -> AuthorDto.builder()
                        .name(a.getName())
                        .birthday(a.getBirthday())
                        .build())
                .collect(Collectors.toSet());

        return BookResponse.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .authors(authorDtos)
                .year(book.getYear())
                .price(book.getPrice())
                .genre(book.getGenre())
                .build();
    }
}
