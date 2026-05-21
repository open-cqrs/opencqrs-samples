package com.example.cqrs.domain;

import com.opencqrs.framework.command.*;
import com.opencqrs.framework.eventhandler.EventHandling;
import com.example.cqrs.domain.UserAccount.Status;
import com.example.cqrs.domain.api.command.*;
import com.example.cqrs.domain.api.exception.*;
import com.example.cqrs.domain.api.event.*;
import org.springframework.beans.factory.annotation.Autowired;

@CommandHandlerConfiguration
public class UserAccountHandling {

    @CommandHandling
    public void handle(SignUpCommand command, CommandEventPublisher<UserAccount> publisher) {
        publisher.publish(new SignUpInitiatedEvent(command.username(), command.email()));
    }

    @StateRebuilding
    public UserAccount on(SignUpInitiatedEvent event) {
        return new UserAccount(event.username(), new Status.Registering(event.email()));
    }

    @EventHandling("user")
    public void on(SignUpInitiatedEvent event, @Autowired CommandRouter router) {
        boolean reserved = router.send(new ReserveEmailAddressCommand(event.email(), event.username()));
        switch (Boolean.valueOf(reserved)) {
            case Boolean granted when granted -> router.send(new CompleteSignUpCommand(event.username(), event.email()));
            case Boolean _ -> router.send(new RejectSignUpCommand(event.username(), event.email()));
        }
    }

    @CommandHandling
    public void handle(UserAccount account, CompleteSignUpCommand command, CommandEventPublisher<UserAccount> publisher) {
        switch (account.status()) {
            case Status.Registering(String email) when email.equalsIgnoreCase(command.email()) ->
                    publisher.publish(new SignUpCompletedEvent(account.username(), email));
            case Status.Registering _ ->
                { /* a completion can only refer to the email the user is actually registering */ }
            case Status.Registered _, Status.ChangingEmail _ ->
                { /* the user is already registered */ }
            case Status.NotRegistered _ ->
                { /* a completion only fires after a successful reservation */ }
        }
    }

    @StateRebuilding
    public UserAccount on(UserAccount account, SignUpCompletedEvent event) {
        return new UserAccount(account.username(), new Status.Registered(event.email()));
    }

    @CommandHandling
    public void handle(UserAccount account, RejectSignUpCommand command, CommandEventPublisher<UserAccount> publisher) {
        switch (account.status()) {
            case Status.Registering _ ->
                    publisher.publish(new SignUpRejectedEvent(account.username(), command.email()));
            case Status.NotRegistered _ ->
                    { /* the sign-up has already been rejected */ }
            case Status.Registered _ ->
                    { /* user is already registered */ }
            case Status.ChangingEmail _ ->
                    { /* only possible after successful sign-up */ }
        }
    }

    @StateRebuilding
    public UserAccount on(UserAccount account, SignUpRejectedEvent event) {
        return new UserAccount(account.username(), new Status.NotRegistered(event.email()));
    }

    @CommandHandling
    public void handle(UserAccount account, ChangeEmailCommand command, CommandEventPublisher<UserAccount> publisher) {
        switch (account.status()) {
            case Status.Registered(String email) when email.equalsIgnoreCase(command.newEmail()) ->
                    throw new SameEmailException();
            case Status.Registered(String email) ->
                    publisher.publish(new EmailChangeInitiatedEvent(account.username(), email, command.newEmail()));
            case Status.ChangingEmail _ -> throw new EmailChangeInProgressException();
            case Status.Registering _ -> throw new SignUpPendingException();
            case Status.NotRegistered _ -> throw new AccountDisabledException();
        }
    }

    @StateRebuilding
    public UserAccount on(UserAccount account, EmailChangeInitiatedEvent event) {
        return new UserAccount(account.username(), new Status.ChangingEmail(event.oldEmail(), event.newEmail()));
    }

    @EventHandling("user")
    public void on(EmailChangeInitiatedEvent event, @Autowired CommandRouter router) {
        boolean reserved = router.send(new ReserveEmailAddressCommand(event.newEmail(), event.username()));
        switch (Boolean.valueOf(reserved)) {
            case Boolean granted when granted ->
                    router.send(new CompleteEmailChangeCommand(event.username()));
            case Boolean _ ->
                    router.send(new RevertEmailChangeCommand(event.username()));
        }
    }

    @CommandHandling
    public void handle(UserAccount account, CompleteEmailChangeCommand command, CommandEventPublisher<UserAccount> publisher) {
        switch (account.status()) {
            case Status.ChangingEmail changing ->
                    publisher.publish(new EmailChangeCompletedEvent(account.username(), changing.email()));
            case Status.Registered _ ->
                    { /* the email change has already been completed */ }
            case Status.Registering _, Status.NotRegistered _ ->
                    { /* only a registered user can start an email change */ }
        }
    }

    @StateRebuilding
    public UserAccount on(UserAccount account, EmailChangeCompletedEvent event) {
        return switch (account.status()) {
            case Status.ChangingEmail changing ->
                    new UserAccount(account.username(), new Status.Registered(changing.newEmail()));
            case Status.Registering _, Status.Registered _, Status.NotRegistered _ -> account;
        };
    }

    @EventHandling("user")
    public void on(EmailChangeCompletedEvent event, @Autowired CommandRouter router) {
        router.send(new ReleaseEmailAddressCommand(event.oldEmail(), event.username()));
    }

    @CommandHandling
    public void handle(UserAccount account, RevertEmailChangeCommand command, CommandEventPublisher<UserAccount> publisher) {
        switch (account.status()) {
            case Status.ChangingEmail _ ->
                    publisher.publish(new EmailChangeRevertedEvent(account.username()));
            case Status.Registered _ ->
                    { /* the revert has already happened */ }
            case Status.Registering _, Status.NotRegistered _ ->
                    { /* only a registered user can start an email change */ }
        }
    }

    @StateRebuilding
    public UserAccount on(UserAccount account, EmailChangeRevertedEvent event) {
        return switch (account.status()) {
            case Status.ChangingEmail changing ->
                    new UserAccount(account.username(), new Status.Registered(changing.email()));
            case Status.Registering _, Status.Registered _, Status.NotRegistered _ -> account;
        };
    }

    @CommandHandling
    public UserAccount handle(UserAccount account, GetUserAccountCommand command) {
        return account;
    }

    @CommandHandling(sourcingMode = SourcingMode.LOCAL)
    public boolean handle(EmailAddress state, ReserveEmailAddressCommand command, CommandEventPublisher<EmailAddress> publisher) {
        final EmailAddressReservedEvent reservedEvent = new EmailAddressReservedEvent(command.email(), command.username());
        return switch (state) {
            case null -> {
                publisher.publish(reservedEvent);
                yield true;
            }
            case EmailAddress.Available _ -> {
                publisher.publish(reservedEvent);
                yield true;
            }
            case EmailAddress.Reserved reserved when reserved.username().equals(command.username()) -> true;
            case EmailAddress.Reserved _ -> false;
        };
    }

    @StateRebuilding
    public EmailAddress on(EmailAddressReservedEvent event) {
        return new EmailAddress.Reserved(event.email(), event.username());
    }

    @CommandHandling
    public void handle(EmailAddress state, ReleaseEmailAddressCommand command, CommandEventPublisher<EmailAddress> publisher) {
        switch (state) {
            case EmailAddress.Reserved reserved when reserved.username().equals(command.username()) ->
                    publisher.publish(new EmailAddressReleasedEvent(command.email()));
            case EmailAddress.Available _ ->
                    { /* the address has already been released */ }
            case EmailAddress.Reserved _ ->
                    { /* the address is reserved by another user */ }
        }
    }

    @StateRebuilding
    public EmailAddress on(EmailAddressReleasedEvent event) {
        return new EmailAddress.Available(event.email());
    }
}
