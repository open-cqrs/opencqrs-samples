package com.example.cqrs.rest;

import com.example.cqrs.domain.api.purchase.PurchaseBookCommand;
import com.example.cqrs.domain.api.rental.LendBookCommand;
import com.example.cqrs.domain.api.rental.ReturnBookCommand;
import com.example.cqrs.service.SynchronizerService;
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
    private final SynchronizerService syncer;

    public BookController(CommandRouter commandRouter, SynchronizerService syncer) {
        this.commandRouter = commandRouter;
        this.syncer = syncer;
    }

    @PostMapping("/purchase")
    public UUID purchase(@RequestBody PurchaseBookCommand command) {
        return commandRouter.send(command);
    }

    @PostMapping("/lend")
    public Object borrow(@RequestBody LendBookCommand command) {
        var correlationID = UUID.randomUUID();

        commandRouter.send(command, Map.ofEntries(Map.entry("correlation-id", correlationID)));

        return syncer.getLatestResultFor(correlationID.toString()).join();
    }

    @PostMapping("/return")
    public void returnBook(@RequestBody ReturnBookCommand command) {
        commandRouter.send(command);
    }
}