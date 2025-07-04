package com.example.cqrs.http;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record LoanDetail(
        @JsonProperty("id") UUID readerId,
        String isbn)
{ }
