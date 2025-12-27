package com.library.loan.client;

import com.library.loan.dto.BookDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "book-service")
public interface BookClient {

    @GetMapping("/api/books/{id}")
    BookDTO getBookById(@PathVariable Long id);

    @PostMapping("/api/books/{id}/decrease")
    Boolean decreaseAvailability(@PathVariable Long id);

    @PostMapping("/api/books/{id}/increase")
    void increaseAvailability(@PathVariable Long id);
}
