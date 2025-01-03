package com.bibin.bookmanagement.repository;

import com.bibin.bookmanagement.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {
    boolean existsByUniqueId(String uniqueId); // Method to check if uniqueId exists
}
