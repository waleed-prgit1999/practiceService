package com.example.travel.common.event;

/**
 * Seam for publishing domain/application events. The Phase 1 implementation just logs; a future
 * phase can add a Kafka/RabbitMQ-backed implementation without changing any caller (see
 * docs/architecture/decisions.md, "Why an event abstraction now?").
 */
public interface DomainEventPublisher {

    void publish(Object event);
}
