package com.example.cqrs.domain.LoanApplication;

import com.example.cqrs.domain.LoanApplication.commands.ApplyLoanRequestCommand;
import com.example.cqrs.domain.LoanApplication.commands.ApproveLoanCommand;
import com.example.cqrs.domain.LoanApplication.events.LoanApplicationAppliedEvent;
import com.example.cqrs.domain.LoanApplication.events.LoanApplicationApprovedEvent;
import com.example.cqrs.domain.LoanApplication.events.LoanApplicationEnrichedEvent;
import com.opencqrs.framework.command.CommandEventPublisher;
import com.opencqrs.framework.command.CommandHandlerConfiguration;
import com.opencqrs.framework.command.CommandHandling;
import com.opencqrs.framework.command.StateRebuilding;
import org.springframework.beans.factory.annotation.Autowired;

@CommandHandlerConfiguration
public class LoanApplicationHandling {

    // --- Command Handlers ---

    @CommandHandling
    public String handle(ApplyLoanRequestCommand command, CommandEventPublisher<LoanRequest> publisher) {
        publisher.publish(
                new LoanApplicationAppliedEvent(
                        command.applicationId(),
                        command.applicant(),
                        command.amount(),
                        command.currency(),
                        command.locationType(),
                        "IN_PERSON".equals(command.locationType())
                )
        );

        return command.getApplicationId();
    }

    @CommandHandling
    public void handle(
            LoanRequest request,
            ApproveLoanCommand command,
            CommandEventPublisher<LoanRequest> publisher,
            @Autowired ManualReviewService manualReviewService) {

        String reviewResult = request.manualReviewResult();
        if (reviewResult == null) {
            // TODO: solution for long running requests - queued event / enriched event
            reviewResult = manualReviewService.fetchReviewResult(command.getApplicationId());
            publisher.publish(new LoanApplicationEnrichedEvent(command.getApplicationId(), reviewResult));
        }

        if (!"COMPLIANT".equals(reviewResult)) {
            throw new IllegalStateException("Loan cannot be approved, review result: " + reviewResult);
        }
        publisher.publish(new LoanApplicationApprovedEvent(command.getApplicationId()));
    }

    // --- State Rebuilding ---

    @StateRebuilding
    public LoanRequest on(LoanApplicationAppliedEvent event) {
        return new LoanRequest(
                event.applicationId(),
                event.applicant(),
                event.amount(),
                event.currency(),
                event.locationType(),
                event.verifiedAddress(),
                null
        );
    }

    @StateRebuilding
    public LoanRequest on(LoanRequest instance, LoanApplicationEnrichedEvent event) {
        return new LoanRequest(
                instance.applicationId(),
                instance.applicant(),
                instance.amount(),
                instance.currency(),
                instance.locationType(),
                instance.verifiedAddress(),
                event.manualReviewResult()
        );
    }

    @StateRebuilding
    public LoanRequest on(LoanRequest instance, LoanApplicationApprovedEvent event) {
        return instance;
    }
}
