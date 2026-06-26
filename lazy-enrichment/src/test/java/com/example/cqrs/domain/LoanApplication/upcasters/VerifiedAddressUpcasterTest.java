package com.example.cqrs.domain.LoanApplication.upcasters;

import com.example.cqrs.domain.LoanApplication.events.LoanApplicationAppliedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencqrs.esdb.client.Event;
import com.opencqrs.framework.serialization.JacksonEventDataMarshaller;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VerifiedAddressUpcasterTest {

    private final VerifiedAddressUpcaster upcaster =
            new VerifiedAddressUpcaster(new JacksonEventDataMarshaller(new ObjectMapper()));

    @Test
    void shouldUpcastLegacyEventInPersonToVerifiedAddressTrue() {
        Event legacy = legacyAppliedEvent(Map.of(
                "applicationId", "app-id",
                "applicant", "Alice",
                "amount", "10000",
                "currency", "EUR",
                "locationType", "IN_PERSON"
        ));

        var results = upcaster.upcast(legacy).toList();

        assertThat(results).singleElement().satisfies(r -> {
            assertThat(r.type()).isEqualTo(LoanApplicationAppliedEvent.class.getName());
            assertThat((Map<String, Object>) r.data().get("payload")).containsEntry("verifiedAddress", true);
        });
    }

    @Test
    void shouldUpcastLegacyEventPostalToVerifiedAddressFalse() {
        Event legacy = legacyAppliedEvent(Map.of(
                "applicationId", "app-id",
                "applicant", "Bob",
                "amount", "20000",
                "currency", "EUR",
                "locationType", "POSTAL"
        ));

        var results = upcaster.upcast(legacy).toList();

        assertThat((Map<String, Object>) results.getFirst().data().get("payload"))
                .containsEntry("verifiedAddress", false);
    }

    @Test
    void shouldNotUpcastEventThatAlreadyHasVerifiedAddress() {
        Event modern = legacyAppliedEvent(Map.of(
                "applicationId", "app-id",
                "applicant", "Carol",
                "amount", "5000",
                "currency", "EUR",
                "locationType", "IN_PERSON",
                "verifiedAddress", true
        ));

        assertThat(upcaster.canUpcast(modern)).isFalse();
    }

    @Test
    void shouldNotUpcastDifferentEventType() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("payload", Map.of("applicationId", "app-id"));
        data.put("metadata", Map.of());
        Event unrelated = new Event(
                "test-source",
                "/loan-applications/app-id",
                "com.example.cqrs.domain.LoanApplication.events.LoanApplicationApprovedEvent",
                data,
                "1.0",
                "evt-1",
                Instant.parse("2024-01-01T00:00:00Z"),
                "application/json",
                null,
                "predecessor"
        );

        assertThat(upcaster.canUpcast(unrelated)).isFalse();
    }

    private static Event legacyAppliedEvent(Map<String, ?> payload) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("payload", payload);
        data.put("metadata", Map.of());
        return new Event(
                "test-source",
                "/loan-applications/" + payload.get("applicationId"),
                LoanApplicationAppliedEvent.class.getName(),
                data,
                "1.0",
                "evt-" + payload.get("applicationId"),
                Instant.parse("2024-01-01T00:00:00Z"),
                "application/json",
                null,
                "predecessor"
        );
    }
}
