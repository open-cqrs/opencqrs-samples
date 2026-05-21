package com.example.cqrs.domain;

import com.example.cqrs.domain.api.command.*;
import com.example.cqrs.domain.api.event.*;
import com.example.cqrs.domain.api.exception.*;
import com.opencqrs.framework.command.CommandHandlingTest;
import com.opencqrs.framework.command.CommandHandlingTestFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@CommandHandlingTest
class UserAccountCommandHandlingTest {

    @Nested
    class CompleteSignUp {

        @Test
        void completesWhenRegistering(@Autowired CommandHandlingTestFixture<CompleteSignUpCommand> fixture) {
            fixture
                    .given().events(new SignUpInitiatedEvent("alice", "alice@example.com"))
                    .when(new CompleteSignUpCommand("alice", "alice@example.com"))
                    .succeeds()
                    .allEvents().exactly(new SignUpCompletedEvent("alice", "alice@example.com"));
        }

        @Test
        void skipsWhenRegisteringForDifferentEmail(@Autowired CommandHandlingTestFixture<CompleteSignUpCommand> fixture) {
            fixture
                    .given().events(new SignUpInitiatedEvent("alice", "alice@example.com"))
                    .when(new CompleteSignUpCommand("alice", "other@example.com"))
                    .succeeds()
                    .withoutEvents();
        }

        @Test
        void skipsWhenAlreadyRegistered(@Autowired CommandHandlingTestFixture<CompleteSignUpCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com")
                    )
                    .when(new CompleteSignUpCommand("alice", "alice@example.com"))
                    .succeeds()
                    .withoutEvents();
        }

        @Test
        void skipsWhenChangingEmail(@Autowired CommandHandlingTestFixture<CompleteSignUpCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com"),
                            new EmailChangeInitiatedEvent("alice", "alice@example.com", "new@example.com")
                    )
                    .when(new CompleteSignUpCommand("alice", "alice@example.com"))
                    .succeeds()
                    .withoutEvents();
        }

        @Test
        void skipsWhenNotRegistered(@Autowired CommandHandlingTestFixture<CompleteSignUpCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpRejectedEvent("alice", "alice@example.com")
                    )
                    .when(new CompleteSignUpCommand("alice", "alice@example.com"))
                    .succeeds()
                    .withoutEvents();
        }
    }

    @Nested
    class RejectSignUp {

        @Test
        void rejectsWhenRegistering(@Autowired CommandHandlingTestFixture<RejectSignUpCommand> fixture) {
            fixture
                    .given().events(new SignUpInitiatedEvent("alice", "alice@example.com"))
                    .when(new RejectSignUpCommand("alice", "alice@example.com"))
                    .succeeds()
                    .allEvents().exactly(new SignUpRejectedEvent("alice", "alice@example.com"));
        }

        @Test
        void skipsWhenAlreadyRejected(@Autowired CommandHandlingTestFixture<RejectSignUpCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpRejectedEvent("alice", "alice@example.com")
                    )
                    .when(new RejectSignUpCommand("alice", "alice@example.com"))
                    .succeeds()
                    .withoutEvents();
        }

        @Test
        void skipsWhenAlreadyRegistered(@Autowired CommandHandlingTestFixture<RejectSignUpCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com")
                    )
                    .when(new RejectSignUpCommand("alice", "alice@example.com"))
                    .succeeds()
                    .withoutEvents();
        }

        @Test
        void skipsWhenChangingEmail(@Autowired CommandHandlingTestFixture<RejectSignUpCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com"),
                            new EmailChangeInitiatedEvent("alice", "alice@example.com", "new@example.com")
                    )
                    .when(new RejectSignUpCommand("alice", "alice@example.com"))
                    .succeeds()
                    .withoutEvents();
        }
    }

    @Nested
    class ChangeEmail {

        @Test
        void publishesInitiatedWhenRegistered(@Autowired CommandHandlingTestFixture<ChangeEmailCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com")
                    )
                    .when(new ChangeEmailCommand("alice", "new@example.com"))
                    .succeeds()
                    .allEvents().exactly(new EmailChangeInitiatedEvent("alice", "alice@example.com", "new@example.com"));
        }

        @Test
        void rejectsSameEmail(@Autowired CommandHandlingTestFixture<ChangeEmailCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com")
                    )
                    .when(new ChangeEmailCommand("alice", "alice@example.com"))
                    .fails().throwing(SameEmailException.class);
        }

        @Test
        void rejectsDuringSignUp(@Autowired CommandHandlingTestFixture<ChangeEmailCommand> fixture) {
            fixture
                    .given().events(new SignUpInitiatedEvent("alice", "alice@example.com"))
                    .when(new ChangeEmailCommand("alice", "new@example.com"))
                    .fails().throwing(SignUpPendingException.class);
        }

        @Test
        void rejectsWhileAnotherChangeInProgress(@Autowired CommandHandlingTestFixture<ChangeEmailCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com"),
                            new EmailChangeInitiatedEvent("alice", "alice@example.com", "pending@example.com")
                    )
                    .when(new ChangeEmailCommand("alice", "other@example.com"))
                    .fails().throwing(EmailChangeInProgressException.class);
        }

        @Test
        void rejectsWhenAccountDisabled(@Autowired CommandHandlingTestFixture<ChangeEmailCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpRejectedEvent("alice", "alice@example.com")
                    )
                    .when(new ChangeEmailCommand("alice", "new@example.com"))
                    .fails().throwing(AccountDisabledException.class);
        }
    }

    @Nested
    class CompleteEmailChange {

        @Test
        void completesWhenChangingEmail(@Autowired CommandHandlingTestFixture<CompleteEmailChangeCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com"),
                            new EmailChangeInitiatedEvent("alice", "alice@example.com", "new@example.com")
                    )
                    .when(new CompleteEmailChangeCommand("alice"))
                    .succeeds()
                    .allEvents().exactly(new EmailChangeCompletedEvent("alice", "alice@example.com"));
        }

        @Test
        void skipsWhenAlreadyCompleted(@Autowired CommandHandlingTestFixture<CompleteEmailChangeCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com")
                    )
                    .when(new CompleteEmailChangeCommand("alice"))
                    .succeeds()
                    .withoutEvents();
        }

        @Test
        void skipsWhenStillRegistering(@Autowired CommandHandlingTestFixture<CompleteEmailChangeCommand> fixture) {
            fixture
                    .given().events(new SignUpInitiatedEvent("alice", "alice@example.com"))
                    .when(new CompleteEmailChangeCommand("alice"))
                    .succeeds()
                    .withoutEvents();
        }

        @Test
        void skipsWhenNotRegistered(@Autowired CommandHandlingTestFixture<CompleteEmailChangeCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpRejectedEvent("alice", "alice@example.com")
                    )
                    .when(new CompleteEmailChangeCommand("alice"))
                    .succeeds()
                    .withoutEvents();
        }
    }

    @Nested
    class RevertEmailChange {

        @Test
        void revertsWhenChangingEmail(@Autowired CommandHandlingTestFixture<RevertEmailChangeCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com"),
                            new EmailChangeInitiatedEvent("alice", "alice@example.com", "new@example.com")
                    )
                    .when(new RevertEmailChangeCommand("alice"))
                    .succeeds()
                    .allEvents().exactly(new EmailChangeRevertedEvent("alice"));
        }

        @Test
        void skipsWhenAlreadyReverted(@Autowired CommandHandlingTestFixture<RevertEmailChangeCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpCompletedEvent("alice", "alice@example.com")
                    )
                    .when(new RevertEmailChangeCommand("alice"))
                    .succeeds()
                    .withoutEvents();
        }

        @Test
        void skipsWhenStillRegistering(@Autowired CommandHandlingTestFixture<RevertEmailChangeCommand> fixture) {
            fixture
                    .given().events(new SignUpInitiatedEvent("alice", "alice@example.com"))
                    .when(new RevertEmailChangeCommand("alice"))
                    .succeeds()
                    .withoutEvents();
        }

        @Test
        void skipsWhenNotRegistered(@Autowired CommandHandlingTestFixture<RevertEmailChangeCommand> fixture) {
            fixture
                    .given().events(
                            new SignUpInitiatedEvent("alice", "alice@example.com"),
                            new SignUpRejectedEvent("alice", "alice@example.com")
                    )
                    .when(new RevertEmailChangeCommand("alice"))
                    .succeeds()
                    .withoutEvents();
        }
    }

    @Nested
    class ReserveEmailAddress {

        @Test
        void reservesNewEmail(@Autowired CommandHandlingTestFixture<ReserveEmailAddressCommand> fixture) {
            fixture
                    .given().nothing()
                    .when(new ReserveEmailAddressCommand("alice@example.com", "alice"))
                    .succeeds()
                    .havingResult(true)
                    .allEvents().exactly(new EmailAddressReservedEvent("alice@example.com", "alice"));
        }

        @Test
        void reservesAvailableEmail(@Autowired CommandHandlingTestFixture<ReserveEmailAddressCommand> fixture) {
            fixture
                    .given().events(
                            new EmailAddressReservedEvent("alice@example.com", "alice"),
                            new EmailAddressReleasedEvent("alice@example.com")
                    )
                    .when(new ReserveEmailAddressCommand("alice@example.com", "bob"))
                    .succeeds()
                    .havingResult(true)
                    .allEvents().exactly(new EmailAddressReservedEvent("alice@example.com", "bob"));
        }

        @Test
        void skipsIdempotentReservationBySameUser(@Autowired CommandHandlingTestFixture<ReserveEmailAddressCommand> fixture) {
            fixture
                    .given().events(new EmailAddressReservedEvent("alice@example.com", "alice"))
                    .when(new ReserveEmailAddressCommand("alice@example.com", "alice"))
                    .succeeds()
                    .havingResult(true)
                    .withoutEvents();
        }

        @Test
        void deniesDifferentUser(@Autowired CommandHandlingTestFixture<ReserveEmailAddressCommand> fixture) {
            fixture
                    .given().events(new EmailAddressReservedEvent("alice@example.com", "alice"))
                    .when(new ReserveEmailAddressCommand("alice@example.com", "bob"))
                    .succeeds()
                    .havingResult(false)
                    .withoutEvents();
        }
    }

    @Nested
    class ReleaseEmailAddress {

        @Test
        void releasesOwnReservation(@Autowired CommandHandlingTestFixture<ReleaseEmailAddressCommand> fixture) {
            fixture
                    .given().events(new EmailAddressReservedEvent("alice@example.com", "alice"))
                    .when(new ReleaseEmailAddressCommand("alice@example.com", "alice"))
                    .succeeds()
                    .allEvents().exactly(new EmailAddressReleasedEvent("alice@example.com"));
        }

        @Test
        void skipsAlreadyReleased(@Autowired CommandHandlingTestFixture<ReleaseEmailAddressCommand> fixture) {
            fixture
                    .given().events(
                            new EmailAddressReservedEvent("alice@example.com", "alice"),
                            new EmailAddressReleasedEvent("alice@example.com")
                    )
                    .when(new ReleaseEmailAddressCommand("alice@example.com", "alice"))
                    .succeeds()
                    .withoutEvents();
        }

        @Test
        void skipsForeignReservation(@Autowired CommandHandlingTestFixture<ReleaseEmailAddressCommand> fixture) {
            fixture
                    .given().events(new EmailAddressReservedEvent("alice@example.com", "alice"))
                    .when(new ReleaseEmailAddressCommand("alice@example.com", "bob"))
                    .succeeds()
                    .withoutEvents();
        }
    }
}
