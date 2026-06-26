package com.example.cqrs.readmodel;

import com.example.cqrs.domain.LoanApplication.events.LoanApplicationAppliedEvent;
import com.example.cqrs.domain.LoanApplication.events.LoanApplicationApprovedEvent;
import com.example.cqrs.domain.LoanApplication.events.LoanApplicationEnrichedEvent;
import com.opencqrs.framework.eventhandler.EventHandling;
import org.springframework.stereotype.Component;

@Component
public class LoanApplicationProjection {

    private final LoanApplicationViewRepository repository;

    public LoanApplicationProjection(LoanApplicationViewRepository repository) {
        this.repository = repository;
    }

    @EventHandling("loan-application-projection")
    public void on(LoanApplicationAppliedEvent event) {
        repository.save(new LoanApplicationView(
                event.applicationId(),
                event.applicant(),
                event.amount(),
                event.currency(),
                event.locationType(),
                event.verifiedAddress()
        ));
    }

    @EventHandling("loan-application-projection")
    public void on(LoanApplicationEnrichedEvent event) {
        repository.findById(event.applicationId()).ifPresent(view -> {
            view.setManualReviewResult(event.manualReviewResult());
            repository.save(view);
        });
    }

    @EventHandling("loan-application-projection")
    public void on(LoanApplicationApprovedEvent event) {
        repository.findById(event.applicationId()).ifPresent(view -> {
            view.setApproved(true);
            repository.save(view);
        });
    }
}
