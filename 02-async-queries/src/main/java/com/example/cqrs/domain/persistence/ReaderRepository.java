package com.example.cqrs.domain.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ReaderRepository extends CrudRepository<ReaderEntity, UUID> {
}
