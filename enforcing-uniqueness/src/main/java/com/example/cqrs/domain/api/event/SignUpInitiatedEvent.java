package com.example.cqrs.domain.api.event;

public record SignUpInitiatedEvent(String username, String email) {}
