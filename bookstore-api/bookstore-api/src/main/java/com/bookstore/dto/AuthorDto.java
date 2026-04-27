package com.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorDto {

    @NotBlank(message = "Author name is required")
    private String name;

    @NotNull(message = "Author birthday is required")
    @PastOrPresent(message = "Birthday cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}
