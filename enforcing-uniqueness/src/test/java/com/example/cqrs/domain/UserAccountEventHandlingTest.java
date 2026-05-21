package com.example.cqrs.domain;

import com.example.cqrs.domain.api.command.*;
import com.example.cqrs.domain.api.event.*;
import com.opencqrs.framework.command.CommandRouter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountEventHandlingTest {

    @Mock
    private CommandRouter router;

    private final UserAccountHandling sut = new UserAccountHandling();

    @Test
    void completesSignUpAfterGrantedReservation() {
        doReturn(true).when(router).send(any(ReserveEmailAddressCommand.class));

        sut.on(new SignUpInitiatedEvent("alice", "alice@example.com"), router);

        InOrder inOrder = inOrder(router);
        inOrder.verify(router).send(new ReserveEmailAddressCommand("alice@example.com", "alice"));
        inOrder.verify(router).send(new CompleteSignUpCommand("alice", "alice@example.com"));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void rejectsSignUpAfterDeniedReservation() {
        doReturn(false).when(router).send(any(ReserveEmailAddressCommand.class));

        sut.on(new SignUpInitiatedEvent("alice", "alice@example.com"), router);

        InOrder inOrder = inOrder(router);
        inOrder.verify(router).send(new ReserveEmailAddressCommand("alice@example.com", "alice"));
        inOrder.verify(router).send(new RejectSignUpCommand("alice", "alice@example.com"));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void completesEmailChangeAfterGrantedReservation() {
        doReturn(true).when(router).send(any(ReserveEmailAddressCommand.class));

        sut.on(new EmailChangeInitiatedEvent("alice", "old@example.com", "new@example.com"), router);

        InOrder inOrder = inOrder(router);
        inOrder.verify(router).send(new ReserveEmailAddressCommand("new@example.com", "alice"));
        inOrder.verify(router).send(new CompleteEmailChangeCommand("alice"));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void revertsEmailChangeAfterDeniedReservation() {
        doReturn(false).when(router).send(any(ReserveEmailAddressCommand.class));

        sut.on(new EmailChangeInitiatedEvent("alice", "old@example.com", "new@example.com"), router);

        InOrder inOrder = inOrder(router);
        inOrder.verify(router).send(new ReserveEmailAddressCommand("new@example.com", "alice"));
        inOrder.verify(router).send(new RevertEmailChangeCommand("alice"));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void releasesOldEmailAfterEmailChangeCompleted() {
        sut.on(new EmailChangeCompletedEvent("alice", "old@example.com"), router);

        verify(router).send(new ReleaseEmailAddressCommand("old@example.com", "alice"));
        verifyNoMoreInteractions(router);
    }
}
