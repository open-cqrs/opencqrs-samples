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
                        entry("identity.user.requested.v1", SignUpInitiatedEvent.class),
                        entry("identity.user.registered.v1", SignUpCompletedEvent.class),
                        entry("identity.user.rejected.v1", SignUpRejectedEvent.class),

                        entry("identity.user.email-change.requested.v1", EmailChangeInitiatedEvent.class),
                        entry("identity.user.email-change.confirmed.v1", EmailChangeCompletedEvent.class),
                        entry("identity.user.email-change.reverted.v1", EmailChangeRevertedEvent.class),

                        entry("identity.email.reserved.v1", EmailAddressReservedEvent.class),
                        entry("identity.email.released.v1", EmailAddressReleasedEvent.class)
                )
        );
    }

    @Bean
    public EventSource eventSource() {
        return new EventSource("tag://reservation-pattern");
    }
}
