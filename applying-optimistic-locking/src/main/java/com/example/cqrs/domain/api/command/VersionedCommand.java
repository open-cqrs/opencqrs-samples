package com.example.cqrs.domain.api.command;

import com.example.cqrs.domain.api.Versioned;
import com.opencqrs.framework.command.Command;

import java.util.ConcurrentModificationException;

public interface VersionedCommand extends Command {
    String expectedVersion();

    default void assertVersionMatches(Versioned state) {
        String expected = expectedVersion();
        if (expected == null || expected.isBlank())
            throw new IllegalArgumentException("expectedVersion must not be blank");
        if (!expected.equals(state.version()))
            throw new ConcurrentModificationException(
                    "Expected version " + expected + " but found " + state.version());
    }
}
