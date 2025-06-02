package com.example.cqrs.async;

import com.opencqrs.framework.CqrsFrameworkException;
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

    public <R> R sendWaitingForEventsHandled(Command command, String group) throws CqrsFrameworkException, InterruptedException {

        var correlationId = UUID.randomUUID().toString();
        var signal = new Object();

        synchronized (signal) {

            MessageHandler messageHandler = m -> {
                var payload = (ProjectorMessage) m.getPayload();

                if (payload.group().equals(group) && payload.correlationId().equals(correlationId)) {
                    synchronized (signal) {
                        signal.notify();
                    }
                }
            };
            channel.subscribe(messageHandler);

            try {
                R result = commandRouter.send(
                        command,
                        Map.of("correlation-id", correlationId)
                );

                signal.wait(5000);

                return result;
            } finally {
                channel.unsubscribe(messageHandler);
            }
        }
    }

    public <R> R sendThenExecute(Command command, String group, Runnable runnable) {
        var correlationId = UUID.randomUUID().toString();
        var unsubscriber = new CompletableFuture<>()
                .orTimeout(5, TimeUnit.SECONDS);

        // Setup message handler
        MessageHandler messageHandler = m -> {
            var payload = (ProjectorMessage) m.getPayload();

            if (payload.group().equals(group) && payload.correlationId().equals(correlationId)) {
                try {
                    runnable.run();
                } finally {
                    unsubscriber.complete(null);
                }
            }
        };
        channel.subscribe(messageHandler);
        unsubscriber.whenComplete((r, ex) -> channel.unsubscribe(messageHandler));

        return commandRouter.send(
                command,
                Map.of("correlation-id", correlationId)
        );
    }
}
