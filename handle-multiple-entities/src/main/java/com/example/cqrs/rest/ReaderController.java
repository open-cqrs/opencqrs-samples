package com.example.cqrs.rest;

import com.example.cqrs.domain.api.purchase.PurchaseBookCommand;
import com.example.cqrs.domain.api.registration.RegisterReaderCommand;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/readers")
public class ReaderController {

    private final CommandRouter router;

    public ReaderController(CommandRouter router) {
        this.router = router;
    }

    @PostMapping
    public String registerReader(@RequestBody ReaderDetail detail) {

        var id = UUID.randomUUID();

        router.send(
                new RegisterReaderCommand(
                        id,
                        detail.firstName(),
                        detail.lastName()
                )
        );

        return id.toString();
    }
}