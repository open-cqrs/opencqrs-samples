package com.example.cqrs.rest;

import com.example.cqrs.domain.api.registration.RegisterReaderCommand;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.example.cqrs.async.CommandBridge;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/readers")
public class ReaderController {

    private final ReaderRepository repository;
    private final CommandBridge bridge;

    public ReaderController(ReaderRepository repository, CommandBridge bridge) {
        this.repository = repository;
        this.bridge = bridge;
    }

    @PostMapping
    public String registerReader(@RequestBody ReaderDetail detail) {

        var id = UUID.randomUUID();

        bridge.sendAndAwait(
                new RegisterReaderCommand(
                        id,
                        detail.firstName(),
                        detail.lastName()
                ),
                "readers"
        );

        return id.toString();
    }

    @GetMapping("/{id}")
    public Set<String> getBooksOfReader(@PathVariable String id) {
        var entity = repository.findById(UUID.fromString(id)).orElseThrow(() -> new IllegalStateException("No such reader registered"));

        return entity.getLentBookISBNs();
    }
}
