package com.example.cqrs.async;

import java.io.Serializable;

public record ProjectorMessage(
        String group,
        String correlationId
) implements Serializable {
}
