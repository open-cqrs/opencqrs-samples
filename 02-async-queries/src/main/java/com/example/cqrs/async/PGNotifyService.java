package com.example.cqrs.async;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.jdbc.channel.PostgresSubscribableChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class PGNotifyService {

    private final PostgresSubscribableChannel channel;

    public PGNotifyService(PostgresSubscribableChannel channel) {
        this.channel = channel;
    }

    public CompletableFuture<Object> queryLatestResultFor(String readModelId, String correlationId, Supplier<Object> query) {
        var futureResult = new CompletableFuture<>();

        MessageHandler messageHandler = m -> {
            var payload = (PGMessagePayload) m.getPayload();

            if (payload.readModelId().equals(readModelId) && payload.correlationId().equals(correlationId)) {
                futureResult.complete(query.get());
            }
        };
        channel.subscribe(messageHandler);

        return futureResult
                .thenApply(
                    result -> {
                        channel.unsubscribe(messageHandler);
                        return result;
                    }
                )
                .orTimeout(5, TimeUnit.SECONDS);
    }
}
