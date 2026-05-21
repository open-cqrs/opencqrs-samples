package com.example.cqrs.domain.api.event;

import java.util.List;

public record BookPurchasedEvent(String isbn, String title, List<String> authors) {}
