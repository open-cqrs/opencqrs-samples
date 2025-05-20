package com.example.cqrs.rest;

import com.example.cqrs.domain.api.purchase.PurchaseBookCommand;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/books")
public class BookController {

    private final CommandRouter router;

    public BookController(CommandRouter router) {
        this.router = router;
    }

    @PostMapping("/purchase")
    public UUID purchase(@RequestBody PurchaseBookCommand command) {
        return router.send(command);
    }
}
