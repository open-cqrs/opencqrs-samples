package com.example.cqrs.domain.persistence;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "READER_OVERVIEW")
public class ReaderEntity {

    @Id
    private UUID id;

    @ElementCollection(fetch = FetchType.EAGER)
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
