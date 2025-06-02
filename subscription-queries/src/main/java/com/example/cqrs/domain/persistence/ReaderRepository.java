package com.example.cqrs.domain.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

/**
 * This {@link org.springframework.data.repository.CrudRepository} serves as a simple projection of the relationship between {@link com.example.cqrs.domain.Reader} and {@link com.example.cqrs.domain.Book}
 */
public interface ReaderRepository extends CrudRepository<ReaderEntity, UUID> {
}
