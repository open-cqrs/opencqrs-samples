package com.example.cqrs.rest;

import com.example.cqrs.domain.api.rental.StartLoanCommand;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final CommandRouter router;

    public LoanController(CommandRouter router) {
        this.router = router;
    }

    @PostMapping
    public void loanBook(@RequestBody LoanDetail detail) {
        var id = UUID.randomUUID();
        router.send(
                new StartLoanCommand(
                        id,
                        detail.readerId(),
                        detail.isbn()
                )
        );
    }

}
