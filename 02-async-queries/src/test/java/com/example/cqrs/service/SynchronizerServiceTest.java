package com.example.cqrs.service;

import com.example.cqrs.async.SynchronizerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class SynchronizerServiceTest {
    private SynchronizerService synchronizerService;

    @BeforeEach
    void setUp() {
        synchronizerService = new SynchronizerService();
    }

    @Test
    public void createsNewFutureIfNotExists() {
        String correlationId = "correlation-id-1";
        CompletableFuture<Object> future = synchronizerService.getLatestResultFor(correlationId);

        assertNotNull(future);
        assertFalse(future.isDone());
    }

    @Test
    public void returnsSameFutureForSameCorrelationId() {
        String correlationId = "correlation-id-2";
        CompletableFuture<Object> firstFuture = synchronizerService.getLatestResultFor(correlationId);
        CompletableFuture<Object> secondFuture = synchronizerService.getLatestResultFor(correlationId);

        assertEquals(firstFuture, secondFuture);
    }

    @Test
    public void completesFutureWithData() throws ExecutionException, InterruptedException, TimeoutException {
        String correlationId = "correlation-id-3";
        String testData = "Test Result";

        CompletableFuture<Object> future = synchronizerService.getLatestResultFor(correlationId);
        synchronizerService.putLatestResultFor(correlationId, testData);

        Object result = future.get(1, TimeUnit.SECONDS);
        assertEquals(testData, result);
    }

    @Test
    public void removesFutureAfterCompletion() {
        String correlationId = "correlation-id-4";
        synchronizerService.getLatestResultFor(correlationId);
        synchronizerService.putLatestResultFor(correlationId, "Test Data");

        CompletableFuture<Object> futureAfterPut = synchronizerService.getLatestResultFor(correlationId);
        assertNotNull(futureAfterPut);
        assertNotEquals(futureAfterPut, CompletableFuture.completedFuture("Test Data"));
    }

    @Test
    public void ignoredForNonExistentCorrelationId() {
        String correlationId = "non-existent-id";
        assertDoesNotThrow(() -> synchronizerService.putLatestResultFor(correlationId, "Test Data"));
    }
}