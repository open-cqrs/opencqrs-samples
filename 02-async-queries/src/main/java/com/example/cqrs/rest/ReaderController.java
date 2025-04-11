package com.example.cqrs.rest;

import com.example.cqrs.domain.api.registration.RegisterReaderCommand;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.example.cqrs.async.PGNotifyService;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/readers")
public class ReaderController {

    private final CommandRouter commandRouter;
    private final ReaderRepository repository;
    private final PGNotifyService notifier;

    public ReaderController(CommandRouter commandRouter, ReaderRepository repository, PGNotifyService notifier) {
        this.commandRouter = commandRouter;
        this.repository = repository;
        this.notifier = notifier;
    }

    @PostMapping
    public String registerReader(@RequestBody ReaderDetail detail) {

        var id = UUID.randomUUID();
        var correlationId = UUID.randomUUID().toString();

        commandRouter.send(
                new RegisterReaderCommand(
                        id,
                        detail.firstName(),
                        detail.lastName()
                ),
                Map.of("correlation-id", correlationId)
        );

        return notifier.queryLatestResultFor(
                "readers",
                correlationId,
                () -> repository.findById(id).get().getId()
        ).join().toString();
    }

    @GetMapping("/{id}")
    public Set<String> getBooksOfReader(@PathVariable String id) {
        var entity = repository.findById(UUID.fromString(id)).orElseThrow(() -> new IllegalStateException("No such reader registered"));

        return entity.getLentBookISBNs();
    }
}
