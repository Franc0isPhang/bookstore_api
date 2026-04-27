-- Sample authors
INSERT INTO authors (id, name, birthday) VALUES (1000, 'George Orwell', '1903-06-25');
INSERT INTO authors (id, name, birthday) VALUES (1001, 'J.K. Rowling', '1965-07-31');
INSERT INTO authors (id, name, birthday) VALUES (1002, 'Stephen King', '1947-09-21');
INSERT INTO authors (id, name, birthday) VALUES (1003, 'Neil Gaiman', '1960-11-10');
INSERT INTO authors (id, name, birthday) VALUES (1004, 'Terry Pratchett', '1948-04-28');

-- Sample books
INSERT INTO books (isbn, title, "year", price, genre) VALUES ('9780451524935', '1984', 1949, 15.99, 'Dystopian');
INSERT INTO books (isbn, title, "year", price, genre) VALUES ('9780747532699', 'Harry Potter and the Philosopher''s Stone', 1997, 20.50, 'Fantasy');
INSERT INTO books (isbn, title, "year", price, genre) VALUES ('9781501142970', 'It', 1986, 22.00, 'Horror');
INSERT INTO books (isbn, title, "year", price, genre) VALUES ('9780060853983', 'Good Omens', 1990, 18.75, 'Fantasy');

-- Book-author mappings
INSERT INTO book_authors (book_isbn, author_id) VALUES ('9780451524935', 1000);
INSERT INTO book_authors (book_isbn, author_id) VALUES ('9780747532699', 1001);
INSERT INTO book_authors (book_isbn, author_id) VALUES ('9781501142970', 1002);
INSERT INTO book_authors (book_isbn, author_id) VALUES ('9780060853983', 1003);
INSERT INTO book_authors (book_isbn, author_id) VALUES ('9780060853983', 1004);


