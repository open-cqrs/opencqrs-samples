package com.example.cqrs.domain.api.purchase;

public record BookPurchasedEvent(
        String isbn,
        String title,
        String author
) {
}
