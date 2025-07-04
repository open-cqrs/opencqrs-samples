package com.example.cqrs.http;

public record PurchaseDetail(
        String isbn,
        String title,
        String author,
        int numPages
) {
}
