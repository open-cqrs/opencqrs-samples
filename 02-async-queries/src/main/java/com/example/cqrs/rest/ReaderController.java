package com.example.cqrs.rest;

import com.example.cqrs.domain.api.registration.RegisterReaderCommand;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.example.cqrs.service.PGNotifyService;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.integration.jdbc.channel.PostgresSubscribableChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    @PostMapping("/register")
    public String registerReader(ReaderDetail detail) {

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
                correlationId,
                () -> repository.findById(id).get().getId()
        ).join().toString();
    }

    @PostMapping("/overview")
    public Set<String> getBooksOfReader(@RequestBody String id) {
        var entity = repository.findById(UUID.fromString(id)).orElseThrow(() -> new IllegalStateException("No such reader registered"));

        return entity.getLentBookISBNs();
    }
}
