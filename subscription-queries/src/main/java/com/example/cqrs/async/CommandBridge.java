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

/**
 * This class is wrapper for {@link com.opencqrs.framework.command.CommandRouter} and can be used in its stead.
 * It provides additional methods for sending commands and then waiting for said commands to be processed asynchronously
 */
@Service
public class CommandBridge {

    private final CommandRouter commandRouter;
    private final SubscribableChannel channel;

    public CommandBridge(CommandRouter commandRouter, SubscribableChannel channel) {
        this.commandRouter = commandRouter;
        this.channel = channel;
    }

    /**
     * @see com.opencqrs.framework.command.CommandRouter#send(Command)
     */
    public <R> R send(Command command) { return commandRouter.send(command); }

    /**
     * @see com.opencqrs.framework.command.CommandRouter#send(Command, Map)
     */
    public <R> R send(Command command, Map<String, ?> metadata) { return commandRouter.send(command, metadata); }

    /**
     *
     * Sends a command and waits for subsequent events having been processed for a specified group
     *
     * @param command the command to be sent to the system
     * @param group the event-handling group for which to wait for callback from
     * @return the return value of the command handler responsible for the command
     * @param <R> the type of the command handlers return value
     * @throws CqrsFrameworkException
     * @throws InterruptedException
     */
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

    /**
     *
     * Sends a command and waits for subsequent events having been processed for a specified group. Then executes a given runnable immediately after.
     *
     * @param command the command to be sent to the system
     * @param group the event-handling group for which to wait for callback from
     * @param runnable the {@link Runnable} to be executed after the callback has been received
     * @return the return value of the command handler responsible for the command
     * @param <R> the type of the command handlers return value
     */
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
