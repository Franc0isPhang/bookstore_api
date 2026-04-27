package com.bookstore.repository;

import com.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN b.authors a " +
           "WHERE (:title IS NULL OR b.title = :title) " +
           "AND (:authorName IS NULL OR a.name = :authorName)")
    List<Book> search(@Param("title") String title,
                      @Param("authorName") String authorName);
}
