package com.opencqrs.rest;

public record PurchaseDetail(
        String isbn,
        String title,
        String author,
        Long numPages
) {
}
