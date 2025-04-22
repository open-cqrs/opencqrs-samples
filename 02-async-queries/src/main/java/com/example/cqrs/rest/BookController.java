package com.example.cqrs.rest;

import com.example.cqrs.domain.api.purchase.PurchaseBookCommand;
import com.example.cqrs.domain.api.rental.LendBookCommand;
import com.example.cqrs.domain.api.rental.ReturnBookCommand;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.example.cqrs.async.CommandBridge;
import com.opencqrs.framework.CqrsFrameworkException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    public SseEmitter borrow(@RequestBody LendBookCommand command) throws InterruptedException, CqrsFrameworkException {
        SseEmitter emitter = new SseEmitter();

        return bridge.sendThenEmitSupplierResult(
                command,
                "readers",
                () -> repository.findById(command.id()).get().getLentBookISBNs().toArray()
        );
    }

    @PostMapping("/return")
    public Object returnBook(@RequestBody ReturnBookCommand command) throws InterruptedException, CqrsFrameworkException {
        return bridge.sendWaitingForSupplierResult(
                command,
                "readers",
                () -> repository.findById(command.id()).get().getLentBookISBNs().toArray()
        );
    }
}