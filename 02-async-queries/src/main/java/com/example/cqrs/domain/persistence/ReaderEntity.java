package com.example.cqrs.domain.persistence;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class ReaderEntity {
    @Id
    public UUID id;

    @ElementCollection
    public List<String> lentBookISBNs = new ArrayList<>();
}
