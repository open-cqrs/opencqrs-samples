package com.example.cqrs.rest;

import com.example.cqrs.domain.api.purchase.PurchaseBookCommand;
import com.example.cqrs.domain.api.rental.LendBookCommand;
import com.example.cqrs.domain.api.rental.ReturnBookCommand;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.example.cqrs.async.CommandBridge;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/books")
public class BookController {

    private final ReaderRepository repository;
    private final CommandBridge bridge;

    public BookController(ReaderRepository repository, CommandBridge bridge) {
        this.repository = repository;
        this.bridge = bridge;
    }

    @PostMapping("/purchase")
    public UUID purchase(@RequestBody PurchaseBookCommand command) {
        return bridge.send(command);
    }

    @PostMapping("/lend")
    public Object borrow(@RequestBody LendBookCommand command) throws InterruptedException {
        bridge.sendWaitingForEventsHandled(
                command,
                "readers"
        );

        return repository.findById(command.id()).get().getLentBookISBNs().toArray();
    }

    @PostMapping("/return")
    public Object returnBook(@RequestBody ReturnBookCommand command) throws InterruptedException {
        bridge.sendWaitingForEventsHandled(
                command,
                "readers"
        );

        return repository.findById(command.id()).get().getLentBookISBNs().toArray();
    }
}