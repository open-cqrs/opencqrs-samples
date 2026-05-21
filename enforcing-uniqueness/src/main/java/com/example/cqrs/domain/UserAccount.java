package com.example.cqrs.domain;

public record UserAccount(String username, Status status) {
    public sealed interface Status {
        record Registering(String email) implements Status {}
        record Registered(String email) implements Status {}
        record ChangingEmail(String email, String newEmail) implements Status {}
        record NotRegistered(String email) implements Status {}
    }
}
