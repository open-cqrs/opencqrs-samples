package com.example.cqrs.domain.api.purchasing;

/**
 *
 * @see PurchaseBookCommand
 *
 * @param isbn ISBN of the purchased book
 * @param title Title of the purchased book
 * @param author Author of the purchased book
 * @param numPages Number of Pages of the purchased book
 */
public record BookInformationAddedEvent(
        String isbn,
        String title,
        String author,
        int numPages
) {
}