package com.example.cqrs.rest;

import com.example.cqrs.domain.api.purchase.PurchaseBookCommand;
import com.example.cqrs.domain.api.rental.LendBookCommand;
import com.example.cqrs.domain.api.rental.ReturnBookCommand;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.example.cqrs.async.PGNotifyService;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/books")
public class BookController {

    private final CommandRouter commandRouter;
    private final ReaderRepository repository;
    private final PGNotifyService notifier;

    public BookController(CommandRouter commandRouter ,ReaderRepository repository, PGNotifyService notifier) {
        this.commandRouter = commandRouter;
        this.repository = repository;
        this.notifier = notifier;
    }

    @PostMapping("/purchase")
    public UUID purchase(@RequestBody PurchaseBookCommand command) {
        return commandRouter.send(command);
    }

    @PostMapping("/lend")
    public Object borrow(@RequestBody LendBookCommand command) {
        var correlationID = UUID.randomUUID().toString();

        commandRouter.send(command, Map.ofEntries(Map.entry("correlation-id", correlationID)));

        return notifier.queryLatestResultFor(
                "readers",
                correlationID,
                () -> repository.findById(command.id()).get().getLentBookISBNs().toArray()).join();
    }

    @PostMapping("/return")
    public Object returnBook(@RequestBody ReturnBookCommand command) {
        var correlationID = UUID.randomUUID().toString();

        commandRouter.send(command, Map.ofEntries(Map.entry("correlation-id", correlationID)));

        return notifier.queryLatestResultFor(
                "readers",
                correlationID,
                () -> repository.findById(command.id()).get().getLentBookISBNs().toArray()).join();
    }
}