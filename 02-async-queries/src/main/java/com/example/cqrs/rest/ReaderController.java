package com.example.cqrs.rest;

import com.example.cqrs.domain.api.registration.RegisterReaderCommand;
import com.example.cqrs.domain.persistence.ReaderRepository;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/readers")
public class ReaderController {

    private final CommandRouter commandRouter;
    private final ReaderRepository repository;

    public ReaderController(CommandRouter commandRouter, ReaderRepository repository) {
        this.commandRouter = commandRouter;
        this.repository = repository;
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

    @PostMapping("/overview")
    public Set<String> getBooksOfReader(@RequestBody String id) {
        var entity = repository.findById(UUID.fromString(id)).orElseThrow(() -> new IllegalStateException("No such reader registered"));

        return entity.getLentBookISBNs();
    }
}
