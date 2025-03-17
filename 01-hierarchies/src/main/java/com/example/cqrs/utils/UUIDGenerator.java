package com.example.cqrs.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UUIDGenerator {
    public UUID getNextUUID() {
        return UUID.randomUUID();
    }
}
