package com.example.cqrs.domain.api.event;

public record EmailChangeInitiatedEvent(String username, String oldEmail, String newEmail) {}
