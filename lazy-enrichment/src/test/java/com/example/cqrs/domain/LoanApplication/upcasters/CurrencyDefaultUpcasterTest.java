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

class CurrencyDefaultUpcasterTest {

    private final CurrencyDefaultUpcaster upcaster =
            new CurrencyDefaultUpcaster(new JacksonEventDataMarshaller(new ObjectMapper()));

    @Test
    void shouldDefaultMissingCurrencyToEur() {
        Event legacy = legacyAppliedEvent(Map.of(
                "applicationId", "app-id",
                "applicant", "Alice",
                "amount", "10000",
                "locationType", "IN_PERSON",
                "verifiedAddress", true
        ));

        var results = upcaster.upcast(legacy).toList();

        assertThat(results).singleElement().satisfies(r -> {
            assertThat((Map<String, Object>) r.data().get("payload")).containsEntry("currency", "EUR");
        });
    }

    @Test
    void shouldNotUpcastEventThatAlreadyHasCurrency() {
        Event modern = legacyAppliedEvent(Map.of(
                "applicationId", "app-id",
                "applicant", "Bob",
                "amount", "20000",
                "currency", "USD",
                "locationType", "POSTAL",
                "verifiedAddress", false
        ));

        assertThat(upcaster.canUpcast(modern)).isFalse();
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
