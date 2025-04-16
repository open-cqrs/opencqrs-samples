package com.example.cqrs.async;

import com.opencqrs.framework.command.Command;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class CommandBridge { // TODO: Rename. Something with Commands and Subscriptions

    private final CommandRouter commandRouter;
    private final SubscribableChannel channel;

    public CommandBridge(CommandRouter commandRouter, SubscribableChannel channel) {
        this.commandRouter = commandRouter;
        this.channel = channel;
    }

    public <R> R send(Command command) { return commandRouter.send(command); }

    public <R> R send(Command command, Map<String, ?> metadata) { return commandRouter.send(command, metadata); }

    public <R> R sendWaitingForEventsHandled(Command command, String group) throws InterruptedException { // TODO: Wait for multiple groups (list)

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

    public <R> R sendWaitingForSupplierResult(Command command, String group, Supplier<R> supplier) {
        var correlationId = UUID.randomUUID().toString();
        CompletableFuture<R> future = new CompletableFuture<>();

        MessageHandler messageHandler = m -> {
            var payload = (ProjectorMessage) m.getPayload();

            if (payload.group().equals(group) && payload.correlationId().equals(correlationId)) {
                    future.complete(supplier.get());
            }
        };
        channel.subscribe(messageHandler);

        commandRouter.send(
                command,
                Map.of("correlation-id", correlationId)
        );

        return future
            .orTimeout(5, TimeUnit.SECONDS)
            .whenComplete((r, ex) -> channel.unsubscribe(messageHandler))
            .join();
    }
}
