package com.bookstore.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private String isbn;
    private String title;
    private Set<AuthorDto> authors;
    private Integer year;
    private Double price;
    private String genre;
}
