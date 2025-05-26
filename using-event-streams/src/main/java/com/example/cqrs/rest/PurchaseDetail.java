package com.example.cqrs.rest;

public record PurchaseDetail(
        String isbn,
        String title,
        String author,
        int numPages
) {
}
