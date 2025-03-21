package com.example.cqrs.rest;

import com.example.cqrs.domain.api.registration.RegisterReaderCommand;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/readers")
public class ReaderController {

    private final CommandRouter commandRouter;

    public ReaderController(CommandRouter commandRouter) {
        this.commandRouter = commandRouter;
    }

    @PostMapping("/register")
    public UUID registerReader(ReaderDetail detail) {

        var id = UUID.randomUUID(); // TODO: Service

        commandRouter.send(
                new RegisterReaderCommand(
                        id,
                        detail.firstName(),
                        detail.lastName()
                )
        );

        return id;
    }
}
