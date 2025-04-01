package com.example.cqrs.service;

import org.springframework.integration.jdbc.channel.PostgresSubscribableChannel;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
public class PGNotifyService {
    private final PostgresSubscribableChannel channel;

    public PGNotifyService(PostgresSubscribableChannel channel) {
        this.channel = channel;
    }

    public CompletableFuture<Object> queryLatestResultFor(String correlationId, Supplier<Object> query) {
        var futureResult = new CompletableFuture<>();

        channel.subscribe(m -> {
            if (m.getPayload().equals(correlationId)) {
                futureResult.complete(query.get());
            }
        });

        return futureResult;
    }
}
