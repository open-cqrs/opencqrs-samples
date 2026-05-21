package com.example.cqrs.domain.api.event;

public record EmailAddressReservedEvent(
        String email,
        String username
) {}
