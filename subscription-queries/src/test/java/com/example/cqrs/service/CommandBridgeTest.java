package com.example.cqrs.service;

import com.example.cqrs.async.CommandBridge;
import com.example.cqrs.async.ProjectorMessage;
import com.example.cqrs.utils.UUIDGenerator;
import com.opencqrs.framework.command.Command;
import com.opencqrs.framework.command.CommandHandlingTest;
import com.opencqrs.framework.command.CommandRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommandBridgeTest {

    @Mock
    private CommandRouter router;

    @Mock
    private SubscribableChannel channel;

    @Mock
    private UUIDGenerator uuidGenerator;

    @Mock private Message<String> message;

    @Mock
    private Command command;

    @Captor
    private ArgumentCaptor<MessageHandler> messageHandlerCaptor;

    private CommandBridge commandBridge;

    @BeforeEach
    void setUp() {
        commandBridge = new CommandBridge(router, channel, uuidGenerator);
    }

    @Test
    public void shouldProcessEventSuccessfully() throws InterruptedException {

        String expectedResult = "test-result";
        String group = "test-group";
        String correlationId = "test-correlation-id";

        when(router.send(eq(command), any(Map.class))).thenReturn(expectedResult);
        when(uuidGenerator.getNextUUIDAsString()).thenReturn(correlationId);

        Message<ProjectorMessage> message = MessageBuilder.withPayload(
                new ProjectorMessage(group, correlationId)
        ).build();

        doAnswer(invocation -> {
            MessageHandler handler = messageHandlerCaptor.getValue();

            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    channel.send(message);

                    // Emulate the internal publish-mechanism of the SubscribableChannel by calling the MessageHandler directly
                    handler.handleMessage(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return null;
        }).when(channel).subscribe(messageHandlerCaptor.capture());

        String result = commandBridge.sendWaitingForEventsHandled(command, group);

        assertEquals(expectedResult, result);
        verify(router).send(eq(command), eq(Map.of("correlation-id", correlationId)));
        verify(channel).subscribe(any(MessageHandler.class));
        verify(channel).send(eq(message));
        verify(channel).unsubscribe(any(MessageHandler.class));
    }

    @Test
    public void shouldExceptOnTimeout() {
        String expectedResult = "test-result";
        String group = "test-group";
        String correlationId = "test-correlation-id";

        when(router.send(eq(command), any(Map.class))).thenReturn(expectedResult);
        when(uuidGenerator.getNextUUIDAsString()).thenReturn(correlationId);

        doAnswer(invocation -> {
            MessageHandler handler = messageHandlerCaptor.getValue();

            new Thread(() -> {
                try {
                    Thread.sleep(5005);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return null;
        }).when(channel).subscribe(messageHandlerCaptor.capture());

        assertThrows(InterruptedException.class, () -> {
            commandBridge.sendWaitingForEventsHandled(command, group);
        });
    }
}
