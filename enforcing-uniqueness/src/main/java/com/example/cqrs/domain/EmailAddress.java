package com.example.cqrs.domain;

public sealed interface EmailAddress {
    record Reserved(String email, String username) implements EmailAddress {}
    record Available(String email)                 implements EmailAddress {}
}
