package com.library.loan.messaging;

import com.library.loan.model.Loan;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoanEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishLoanCreated(Loan loan) {
        publishEvent("LOAN_CREATED", loan);
    }

    public void publishLoanReturned(Loan loan) {
        publishEvent("LOAN_RETURNED", loan);
    }

    private void publishEvent(String eventType, Loan loan) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("loan", loan);
        rabbitTemplate.convertAndSend(RabbitMQConfig.LOAN_EVENTS_QUEUE, event);
    }
}
