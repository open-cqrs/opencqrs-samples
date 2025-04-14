package com.example.cqrs.async;

import com.example.cqrs.domain.api.registration.RegisterReaderCommand;
import com.opencqrs.framework.command.Command;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class CommandBridge {

    private final CommandRouter commandRouter;
    private final SubscribableChannel channel;

    public CommandBridge(CommandRouter commandRouter, SubscribableChannel channel) {
        this.commandRouter = commandRouter;
        this.channel = channel;
    }

    public <R> R send(Command command) { return commandRouter.send(command); }

    public <R> R send(Command command, Map<String, ?> metadata) { return commandRouter.send(command, metadata); }

    public <R> R sendAndAwait(Command command, String readModelId) {

        var correlationId = UUID.randomUUID().toString();
        var future = new CompletableFuture<>();

        MessageHandler messageHandler = m -> {
            var payload = (ProjectorMessage) m.getPayload();

            if (payload.readModelId().equals(readModelId) && payload.correlationId().equals(correlationId)) {
                future.complete(null);
            }
        };
        channel.subscribe(messageHandler);

        R result = commandRouter.send(
                command,
                Map.of("correlation-id", correlationId)
        );

        return future
            .thenApply(
                ignored -> { // TODO: Idiomatic?
                    channel.unsubscribe(messageHandler);
                    return result;
                }
            )
            .orTimeout(5, TimeUnit.SECONDS)
            .join();
    }
}
