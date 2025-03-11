package com.example.cqrs.services;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UUIDGenerator {
    public UUID getNextUUID() {
        return UUID.randomUUID();
    }
}
