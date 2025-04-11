package com.example.cqrs.async;

import java.io.Serializable;

public record PGMessagePayload(
        String readModelId,
        String correlationId
) implements Serializable {
}
