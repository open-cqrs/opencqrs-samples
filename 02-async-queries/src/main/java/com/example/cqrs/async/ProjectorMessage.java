package com.example.cqrs.async;

import java.io.Serializable;

public record ProjectorMessage(
        String readModelId,
        String correlationId
) implements Serializable {
}
