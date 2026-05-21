package com.example.cqrs.domain.api.event;

public record SignUpCompletedEvent(String username, String email) {}
