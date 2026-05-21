package com.example.cqrs.domain.api.event;

public record EmailChangeCompletedEvent(String username, String oldEmail) {}
