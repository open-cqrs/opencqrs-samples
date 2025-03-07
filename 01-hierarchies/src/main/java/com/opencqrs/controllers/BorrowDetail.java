package com.opencqrs.controllers;

import java.util.UUID;

public record BorrowDetail(
        String id,
        String isbn
) {
}
