package com.example.cqrs.domain.LoanApplication.upcasters;

import com.example.cqrs.domain.LoanApplication.events.LoanApplicationAppliedEvent;
import com.opencqrs.esdb.client.Event;
import com.opencqrs.framework.serialization.EventDataMarshaller;
import com.opencqrs.framework.upcaster.AbstractEventDataMarshallingEventUpcaster;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Compensate strategy: supply a default {@code currency} for events written before the field existed.
 *
 * <p>Old {@link LoanApplicationAppliedEvent} payloads did not carry a {@code currency} field; the
 * business at that time operated in a single currency. {@code "EUR"} is the meaningful default — it
 * reflects historical reality for all events written before the field was introduced. The upcaster
 * fills the gap at read time so the application code can treat currency as a present-day required
 * field.
 */
public class CurrencyDefaultUpcaster extends AbstractEventDataMarshallingEventUpcaster {

    public CurrencyDefaultUpcaster(EventDataMarshaller marshaller) {
        super(marshaller);
    }

    @Override
    public boolean canUpcast(Event event) {
        if (!event.type().equals(LoanApplicationAppliedEvent.class.getName())) return false;
        Object payload = event.data().get("payload");
        return payload instanceof Map<?, ?> p && !p.containsKey("currency");
    }

    @Override
    protected Stream<MetaDataAndPayloadResult> doUpcast(Event event, Map<String, ?> metaData, Map<String, ?> payload) {
        Map<String, Object> upcasted = new HashMap<>(payload);
        upcasted.put("currency", "EUR");
        return Stream.of(new MetaDataAndPayloadResult(event.type(), metaData, upcasted));
    }
}
