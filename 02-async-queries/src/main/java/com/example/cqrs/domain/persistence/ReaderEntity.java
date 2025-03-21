package com.example.cqrs.domain.persistence;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.*;

@Entity
@Table(name = "READER_OVERVIEW")
public class ReaderEntity {

    @Id
    private UUID id;

    @ElementCollection
    private Set<String> lentBookISBNs = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Set<String> getLentBookISBNs() {
        return lentBookISBNs;
    }
}
