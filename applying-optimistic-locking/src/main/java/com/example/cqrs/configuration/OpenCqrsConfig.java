package com.example.cqrs.configuration;

import com.opencqrs.framework.persistence.EventSource;
import com.opencqrs.framework.types.PreconfiguredAssignableClassEventTypeResolver;
import com.example.cqrs.domain.api.event.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import static java.util.Map.entry;

@Configuration
public class OpenCqrsConfig {
    @Bean
    public PreconfiguredAssignableClassEventTypeResolver eventTypeResolver() {
        return new PreconfiguredAssignableClassEventTypeResolver(
                Map.ofEntries(
                        entry("catalog.book.purchased.v1", BookPurchasedEvent.class),
                        entry("catalog.book.details-edited.v1", BookDetailsEditedEvent.class)
                )
        );
    }

    @Bean
    public EventSource eventSource() {
        return new EventSource("tag://optimistic-locking-demo");
    }
}
