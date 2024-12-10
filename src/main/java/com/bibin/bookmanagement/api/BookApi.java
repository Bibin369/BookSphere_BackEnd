package com.bibin.bookmanagement.api;

import com.bibin.bookmanagement.dto.ApiResponse;
import com.bibin.bookmanagement.dto.BookCreateDto;
import com.bibin.bookmanagement.model.Book;
import com.bibin.bookmanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/book")
public class BookApi {
    @Autowired
    private BookService bookService;

    @GetMapping("/listAll")
    public ResponseEntity<ApiResponse> listAllBooks(){
        List<Book> bookList = bookService.bookList();

        if (bookList.isEmpty()){
            return ResponseEntity.ok(new ApiResponse("success","There are no books",null));
        }
        return ResponseEntity.ok(new ApiResponse("success",bookList,null));
    }

    @PostMapping("/new")
    public ResponseEntity<ApiResponse> createNewBook(@RequestBody BookCreateDto bookCreateDto){
        try {
            Book book = bookService.createBook(bookCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("success","book created with title ",null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error",e.getMessage(),null));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable String id){
        try {
            bookService.deleteBookWithId(id);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("success","book deleted with id "+id,null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error",e.getMessage(),null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBookById(@PathVariable String id) {
        try {
            Book book = bookService.findBookById(id);

            Map<String, Object> bookDetails = new HashMap<>();
            bookDetails.put("title", book.getTitle());
            bookDetails.put("author", book.getAuthor());
            bookDetails.put("genre", book.getGenre());
            bookDetails.put("publicationDate", book.getPublicationDate());
            bookDetails.put("photoUrl", book.getPhotoUrl());

            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponse("success",bookDetails, "Book details retrieved successfully")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse("error", e.getMessage(), null)
            );
        }
    }

    // New endpoint to upload photo for a specific book by ID
    @PostMapping("/uploadPhoto/{id}")
    public ResponseEntity<ApiResponse> uploadPhotoToBook(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        try {
            String photoUrl = bookService.uploadPhoto(id, file); // Pass file to the service method
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("success", "Photo uploaded successfully", photoUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage(), null));
        }
    }






}
