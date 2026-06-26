package com.example.cqrs.domain.LoanApplication;

import com.example.cqrs.domain.LoanApplication.commands.ApplyLoanRequestCommand;
import com.example.cqrs.domain.LoanApplication.commands.ApproveLoanCommand;
import com.example.cqrs.domain.LoanApplication.events.LoanApplicationAppliedEvent;
import com.example.cqrs.domain.LoanApplication.events.LoanApplicationApprovedEvent;
import com.example.cqrs.domain.LoanApplication.events.LoanApplicationEnrichedEvent;
import com.opencqrs.framework.command.CommandHandlingTest;
import com.opencqrs.framework.command.CommandHandlingTestFixture;
import com.opencqrs.framework.command.CommandSubjectAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

@CommandHandlingTest
class LoanApplicationHandlingTest {

    @MockitoBean
    ManualReviewService manualReviewService;

    @Test
    void shouldSubmitNewLoanApplication(@Autowired CommandHandlingTestFixture<ApplyLoanRequestCommand> fixture) {
        var command = new ApplyLoanRequestCommand("applicant-1", "10000", "EUR", "IN_PERSON");

        fixture.givenNothing()
                .when(command)
                .expectSuccessfulExecution()
                .expectSingleEvent(new LoanApplicationAppliedEvent(
                        command.getApplicationId(), "applicant-1", "10000", "EUR", "IN_PERSON", true
                ));
    }

    @Test
    void shouldRejectDuplicateApplication(@Autowired CommandHandlingTestFixture<ApplyLoanRequestCommand> fixture) {
        fixture.given(new LoanApplicationAppliedEvent(
                        "app-id", "applicant-1", "10000", "EUR", "POSTAL", false))
                .when(new ApplyLoanRequestCommand("app-id", "applicant-1", "10000", "EUR", "POSTAL"))
                .expectException(CommandSubjectAlreadyExistsException.class);
    }

    @Test
    void shouldEnrichAndApproveInOneAppendWhenNotYetEnriched(
            @Autowired CommandHandlingTestFixture<ApproveLoanCommand> fixture) {
        when(manualReviewService.fetchReviewResult("app-id")).thenReturn("COMPLIANT");

        fixture.given(new LoanApplicationAppliedEvent(
                        "app-id", "applicant-1", "10000", "EUR", "POSTAL", false))
                .when(new ApproveLoanCommand("app-id"))
                .expectSuccessfulExecution()
                .expectEvents(
                        new LoanApplicationEnrichedEvent("app-id", "COMPLIANT"),
                        new LoanApplicationApprovedEvent("app-id")
                );
    }

    @Test
    void shouldOnlyApproveWhenAlreadyEnriched(
            @Autowired CommandHandlingTestFixture<ApproveLoanCommand> fixture) {
        fixture.given(
                        new LoanApplicationAppliedEvent(
                                "app-id", "applicant-1", "10000", "EUR", "POSTAL", false),
                        new LoanApplicationEnrichedEvent("app-id", "COMPLIANT")
                )
                .when(new ApproveLoanCommand("app-id"))
                .expectSuccessfulExecution()
                .expectSingleEvent(new LoanApplicationApprovedEvent("app-id"));
    }

    @Test
    void shouldRejectApprovalWhenReviewResultIsNotCompliant(
            @Autowired CommandHandlingTestFixture<ApproveLoanCommand> fixture) {
        when(manualReviewService.fetchReviewResult("app-id")).thenReturn("REJECTED");

        fixture.given(new LoanApplicationAppliedEvent(
                        "app-id", "applicant-1", "10000", "EUR", "POSTAL", false))
                .when(new ApproveLoanCommand("app-id"))
                .expectException(IllegalStateException.class);
    }
}
