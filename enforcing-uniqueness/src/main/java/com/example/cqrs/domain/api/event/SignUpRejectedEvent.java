package com.example.cqrs.domain.api.event;

public record SignUpRejectedEvent(String username, String email) {}
