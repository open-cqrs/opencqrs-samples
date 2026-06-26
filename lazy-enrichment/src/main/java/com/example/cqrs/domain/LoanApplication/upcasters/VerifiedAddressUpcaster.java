package com.example.cqrs.domain.LoanApplication.upcasters;

import com.example.cqrs.domain.LoanApplication.events.LoanApplicationAppliedEvent;
import com.opencqrs.esdb.client.Event;
import com.opencqrs.framework.serialization.EventDataMarshaller;
import com.opencqrs.framework.upcaster.AbstractEventDataMarshallingEventUpcaster;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Calculate strategy: derive {@code verifiedAddress} from the existing {@code locationType} field.
 *
 * <p>Old {@link LoanApplicationAppliedEvent} payloads were written before the boolean
 * {@code verifiedAddress} field existed. The value is fully determined by the {@code locationType}
 * that was already recorded — in-person verification implies a verified address, postal does not.
 * The upcaster fills the field at read time so application code only ever sees the current schema.
 */
public class VerifiedAddressUpcaster extends AbstractEventDataMarshallingEventUpcaster {

    public VerifiedAddressUpcaster(EventDataMarshaller marshaller) {
        super(marshaller);
    }

    @Override
    public boolean canUpcast(Event event) {
        if (!event.type().equals(LoanApplicationAppliedEvent.class.getName())) return false;
        Object payload = event.data().get("payload");
        return payload instanceof Map<?, ?> p && !p.containsKey("verifiedAddress");
    }

    @Override
    protected Stream<MetaDataAndPayloadResult> doUpcast(Event event, Map<String, ?> metaData, Map<String, ?> payload) {
        Map<String, Object> upcasted = new HashMap<>(payload);
        upcasted.put("verifiedAddress", "IN_PERSON".equals(payload.get("locationType")));
        return Stream.of(new MetaDataAndPayloadResult(event.type(), metaData, upcasted));
    }
}
