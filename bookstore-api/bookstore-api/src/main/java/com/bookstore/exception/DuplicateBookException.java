package com.bookstore.exception;

public class DuplicateBookException extends RuntimeException {
    public DuplicateBookException(String isbn) {
        super("Book already exists with ISBN: " + isbn);
    }
}
