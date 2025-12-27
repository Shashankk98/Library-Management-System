package com.library.loan.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String LOAN_EVENTS_QUEUE = "loan.events";

    @Bean
    public Queue loanEventsQueue() {
        return new Queue(LOAN_EVENTS_QUEUE, true);
    }
}
