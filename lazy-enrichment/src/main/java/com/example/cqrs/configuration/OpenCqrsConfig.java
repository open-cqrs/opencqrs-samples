package com.example.cqrs.configuration;

import com.example.cqrs.domain.LoanApplication.upcasters.CurrencyDefaultUpcaster;
import com.example.cqrs.domain.LoanApplication.upcasters.VerifiedAddressUpcaster;
import com.opencqrs.framework.serialization.EventDataMarshaller;
import com.opencqrs.framework.upcaster.EventUpcaster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenCqrsConfig {

    @Bean
    public EventUpcaster verifiedAddressUpcaster(EventDataMarshaller marshaller) {
        return new VerifiedAddressUpcaster(marshaller);
    }

    @Bean
    public EventUpcaster currencyDefaultUpcaster(EventDataMarshaller marshaller) {
        return new CurrencyDefaultUpcaster(marshaller);
    }
}
