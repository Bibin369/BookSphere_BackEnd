package com.bibin.bookmanagement.service;

import com.bibin.bookmanagement.dto.BookCreateDto;
import com.bibin.bookmanagement.model.Book;
import com.bibin.bookmanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    private static int counter = 1;  // Counter to generate unique sequence-based ID
    private final String PHOTO_UPLOAD_DIR = "E:\\BookManagementSystem\\FrontEnd\\book_management_system_frontend\\public\\images\\upload\\directory\\";

    public Book createBook(BookCreateDto bookCreateDto) {
        // Validate title (max 100 characters, required)
        if (bookCreateDto.title() == null || bookCreateDto.title().isEmpty() || bookCreateDto.title().length() > 100) {
            throw new IllegalArgumentException("Title is required and must not exceed 100 characters.");
        }

        // Validate author (max 50 characters, required)
        if (bookCreateDto.author() == null || bookCreateDto.author().isEmpty() || bookCreateDto.author().length() > 50) {
            throw new IllegalArgumentException("Author is required and must not exceed 50 characters.");
        }

        // Validate publication date (required)
        if (bookCreateDto.publicationDate() == null) {
            throw new IllegalArgumentException("Publication date is required.");
        }

        // Validate ISBN (13-digit number, required)
        if (bookCreateDto.isbn() == null || bookCreateDto.isbn().length() != 13 || !bookCreateDto.isbn().matches("\\d+")) {
            throw new IllegalArgumentException("ISBN must be a 13-digit number.");
        }

        // Validate genre (Dropdown options)
        String genre = bookCreateDto.genre();
        List<String> validGenres = List.of("Fiction", "Non-Fiction", "Mystery", "Fantasy", "Romance", "Sci-Fi", "Others");
        if (genre == null || !validGenres.contains(genre)) {
            throw new IllegalArgumentException("Invalid genre selected.");
        }

        // Validate rating (1 to 5, required)
        if (bookCreateDto.rating() < 1 || bookCreateDto.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        // If all validations pass, create the book
        Book book = new Book();
        book.setAuthor(bookCreateDto.author());
        book.setTitle(bookCreateDto.title());
        book.setIsbn(bookCreateDto.isbn());
        book.setRating(bookCreateDto.rating());
        book.setGenre(bookCreateDto.genre());
        book.setPublicationDate(bookCreateDto.publicationDate());
        book.setUniqueId(createUniqueId());

        try {
            return bookRepository.save(book);
        } catch (RuntimeException e) {
            throw new RuntimeException("Couldn't save book, " + e.getMessage());
        }
    }


    // Generates unique ID in the format B-001, B-002, etc.
    private String createUniqueId() {
        String uniqueId;
        do {
            uniqueId = String.format("B-%03d", counter++);
        } while (bookRepository.existsByUniqueId(uniqueId)); // Check if the ID already exists
        return uniqueId;
    }

    // Method to delete a book by its ID
    public void deleteBookWithId(String id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Couldn't find book with id: " + id)
        );

        try {
            bookRepository.delete(book);
        } catch (RuntimeException e) {
            throw new RuntimeException("Cannot delete book with id: " + id + " cause: " + e.getMessage());
        }
    }

    // Method to list all books
    public List<Book> bookList() {
        return bookRepository.findAll();
    }

    // Method to find a book by its unique ID
    public Book findBookById(String id) {
        return bookRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Book not found with ID: " + id)
        );
    }

    // Method to upload a photo for a specific book by ID
    public String uploadPhoto(String bookId, MultipartFile file) {
        // Find the book by ID
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();

            try {
                // Absolute path for the upload directory (ensure this exists on your system)
                String uploadDir = "E:\\BookManagementSystem\\FrontEnd\\book_management_system_frontend\\public\\images\\upload\\directory\\"; // Updated path
                Path uploadPath = Paths.get(uploadDir);

                // Ensure the upload directory exists
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);  // Create directory if it doesn't exist
                }

                // Generate a unique file name to avoid overwriting
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                // Store the file locally
                file.transferTo(filePath.toFile());

                // Update the book's photo URL (relative path for access)
                String photoUrl = "/uploads/photos/" + fileName;
                book.setPhotoUrl(photoUrl);
                bookRepository.save(book); // Save the book with the updated photo URL

                return photoUrl; // Return the relative URL of the uploaded image
            } catch (IOException e) {
                throw new RuntimeException("File upload failed: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Book not found with ID: " + bookId);
        }
    }




}
