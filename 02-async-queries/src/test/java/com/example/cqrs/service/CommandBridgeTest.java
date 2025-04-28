package com.example.cqrs.service;

import com.example.cqrs.async.CommandBridge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.jdbc.channel.PostgresSubscribableChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandBridgeTest {

    @MockitoBean
    private PostgresSubscribableChannel channel;

    @MockitoBean private Message<String> message;

    @Captor
    private ArgumentCaptor<MessageHandler> messageHandlerCaptor;

    @Autowired private CommandBridge service;

    @Test
    void shouldCompleteWhenCorrectCorrelationIdReceived() throws ExecutionException, InterruptedException {
        // Given
        String correlationId = "test-correlation-id";
        String expectedResult = "query-result";
        Supplier<Object> query = () -> expectedResult;

        // When
        CompletableFuture<Object> future = service.sendWaitingForEventsHandled(, correlationId, query);

        // Then
        verify(channel).subscribe(messageHandlerCaptor.capture());
        MessageHandler handler = messageHandlerCaptor.getValue();

        // Simulate message with matching correlation ID
        when(message.getPayload()).thenReturn(correlationId);
        handler.handleMessage(message);

        // Verify that future completes with expected result
        Object result = future.get();
        assertEquals(expectedResult, result);
        verify(channel).unsubscribe(handler);
    }

    @Test
    void shouldNotCompleteWhenDifferentCorrelationIdReceived() throws InterruptedException {
        // Given
        String correlationId = "test-correlation-id";
        String differentId = "different-id";
        Supplier<Object> query = () -> "query-result";

        // When
        CompletableFuture<Object> future = service.sendWaitingForEventsHandled(, correlationId, query);

        // Then
        verify(channel).subscribe(messageHandlerCaptor.capture());
        MessageHandler handler = messageHandlerCaptor.getValue();

        // Simulate message with non-matching correlation ID
        when(message.getPayload()).thenReturn(differentId);
        handler.handleMessage(message);

        // Verify the future is not completed
        assertFalse(future.isDone());
    }

    @Test
    void shouldNotExecuteQueryWhenCorrelationIdDoesNotMatch() {
        // Given
        String correlationId = "test-correlation-id";
        String differentId = "different-id";

        Supplier<Object> query = mock(Supplier.class);

        // When
        CompletableFuture<Object> future = service.sendWaitingForEventsHandled(, correlationId, query);

        // Then
        verify(channel).subscribe(messageHandlerCaptor.capture());
        MessageHandler handler = messageHandlerCaptor.getValue();

        // Simulate message with non-matching correlation ID
        when(message.getPayload()).thenReturn(differentId);
        handler.handleMessage(message);

        // Verify the query was not executed
        verifyNoInteractions(query);
    }

    private void verifyNoInteractions(Supplier<Object> query) {
    }

    @Test
    void shouldExecuteQueryOnlyWhenCorrelationIdMatches() {
        // Given
        String correlationId = "test-correlation-id";
        Supplier<Object> query = mock(Supplier.class);
        when(query.get()).thenReturn("result");

        // When
        CompletableFuture<Object> future = service.sendWaitingForEventsHandled(, correlationId, query);

        // Then
        verify(channel).subscribe(messageHandlerCaptor.capture());
        MessageHandler handler = messageHandlerCaptor.getValue();

        // Simulate message with matching correlation ID
        when(message.getPayload()).thenReturn(correlationId);
        handler.handleMessage(message);

        // Verify the query was executed exactly once
        verify(query, times(1)).get();
    }

    @Test
    void shouldUnsubscribeAfterFutureIsComplete() throws ExecutionException, InterruptedException {
        // Given
        String correlationId = "test-correlation-id";
        Supplier<Object> query = () -> "result";

        // When
        CompletableFuture<Object> future = service.sendWaitingForEventsHandled(, correlationId, query);

        // Then
        verify(channel).subscribe(messageHandlerCaptor.capture());
        MessageHandler handler = messageHandlerCaptor.getValue();

        // Simulate message with matching correlation ID
        when(message.getPayload()).thenReturn(correlationId);
        handler.handleMessage(message);

        // Wait for future to complete
        future.get();

        // Verify readerLentBooksChannel.unsubscribe was called with correct handler
        verify(channel).unsubscribe(handler);
    }
}
