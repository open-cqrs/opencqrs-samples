package com.example.cqrs.async;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SynchronizerService {
    private final Map<String, CompletableFuture<Object>> futureResults = new ConcurrentHashMap<>();

    public CompletableFuture<Object> getLatestResultFor(String correlationId) {
        return futureResults.computeIfAbsent(correlationId, k -> new CompletableFuture<>());
    }

    public void putLatestResultFor(String correlationId, Object data) {
        if (futureResults.containsKey(correlationId)) {
            futureResults.get(correlationId).complete(data);
            futureResults.remove(correlationId);
        }
    }
}
