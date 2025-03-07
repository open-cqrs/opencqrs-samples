package com.opencqrs.rest;

import com.opencqrs.domain.api.borrowing.BorrowBookCommand;
import com.opencqrs.domain.api.purchasing.PurchaseBookCommand;
import com.opencqrs.domain.api.returning.ReturnBookCommand;
import de.dxfrontiers.cqrs.framework.command.CommandRouter;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/books")
public class BookController {

    private final CommandRouter commandRouter;

    public BookController(CommandRouter commandRouter) {
        this.commandRouter = commandRouter;
    }

    @PostMapping("/purchase")
    public UUID purchase(@RequestBody PurchaseDetail detail) {
        var command =
                new PurchaseBookCommand(
                        detail.isbn(),
                        detail.title(),
                        detail.author(),
                        detail.numPages()
                );

        commandRouter.send(command);

        return command.id();
    }

    @PostMapping("/borrow")
    public void borrow(@RequestBody BorrowDetail detail) {
        var command =
                new BorrowBookCommand(
                        UUID.fromString(detail.id()),
                        detail.isbn()
                );

        commandRouter.send(command);
    }

    @PostMapping("/return")
    public void returnBook(@RequestBody BorrowDetail detail) {
        var command =
                new ReturnBookCommand(
                        UUID.fromString(detail.id()),
                        detail.isbn()
                );

        commandRouter.send(command);
    }
}