package com.bookstore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @NotBlank(message = "ISBN is required")
    @Pattern(
        regexp = "^(97(8|9))?\\d{9}(\\d|X)$",
        message = "ISBN must be a valid ISBN-10 or ISBN-13"
    )
    private String isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @NotEmpty(message = "At least one author is required")
    @Valid
    private Set<AuthorDto> authors;

    @NotNull(message = "Year is required")
    @Min(value = 1450, message = "Year must be 1450 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private Integer year;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    @NotBlank(message = "Genre is required")
    private String genre;
}
