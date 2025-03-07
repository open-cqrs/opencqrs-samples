package com.opencqrs.controllers;

public record PurchaseDetail(
        String isbn,
        String title,
        String author,
        Long numPages
) {
}
