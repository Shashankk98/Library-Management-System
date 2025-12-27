package com.library.book.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.book.model.Book;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BookEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishBookCreated(Book book) {
        publishEvent("BOOK_CREATED", book);
    }

    public void publishBookUpdated(Book book) {
        publishEvent("BOOK_UPDATED", book);
    }

    public void publishBookDeleted(Long bookId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "BOOK_DELETED");
        event.put("bookId", bookId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.BOOK_EVENTS_QUEUE, event);
    }

    private void publishEvent(String eventType, Book book) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("book", book);
        rabbitTemplate.convertAndSend(RabbitMQConfig.BOOK_EVENTS_QUEUE, event);
    }
}
