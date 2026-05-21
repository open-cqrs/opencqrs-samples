package com.example.cqrs.http;

import com.opencqrs.framework.command.CommandRouter;
import com.example.cqrs.domain.UserAccount;
import com.example.cqrs.domain.api.command.ChangeEmailCommand;
import com.example.cqrs.domain.api.command.GetUserAccountCommand;
import com.example.cqrs.domain.api.command.SignUpCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-accounts")
public class UserController {

    private final CommandRouter commandRouter;

    public UserController(CommandRouter commandRouter) {
        this.commandRouter = commandRouter;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserAccount> getAccount(@PathVariable String username) {
        return ResponseEntity.ok(commandRouter.send(new GetUserAccountCommand(username)));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody SignUpCommand command) {
        commandRouter.send(new SignUpCommand(command.username().toLowerCase(), command.email().toLowerCase()));
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{username}/change-email")
    public ResponseEntity<Void> changeEmail(@PathVariable String username, @RequestBody ChangeEmailCommand command) {
        commandRouter.send(new ChangeEmailCommand(username, command.newEmail().toLowerCase()));
        return ResponseEntity.accepted().build();
    }
}
