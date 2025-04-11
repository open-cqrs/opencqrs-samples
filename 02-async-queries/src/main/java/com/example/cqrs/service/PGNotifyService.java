package com.example.cqrs.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.jdbc.channel.PostgresSubscribableChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
public class PGNotifyService {

    private final Map<String, PostgresSubscribableChannel> channels;

    public PGNotifyService(Map<String, PostgresSubscribableChannel> channels) {
        this.channels = channels;
    }

    public void sendMessageFor(String channelId,  Message<String> message) {
        channels.get(channelId).send(message);
    }

    public CompletableFuture<Object> queryLatestResultFor(String channelId, String correlationId, Supplier<Object> query) {
        var futureResult = new CompletableFuture<>();

        var channel = channels.get(channelId);
        MessageHandler messageHandler = m -> {
            if (m.getPayload().equals(correlationId)) {
                futureResult.complete(query.get());
            }
        };
        channel.subscribe(messageHandler);

        // TODO: Timeout as well?

        return futureResult.thenApply(
                result -> {
                    channel.unsubscribe(messageHandler);
                    return result;
                }
        );
    }
}
