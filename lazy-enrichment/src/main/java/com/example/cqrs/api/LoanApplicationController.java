package com.example.cqrs.api;

import com.example.cqrs.domain.LoanApplication.commands.ApplyLoanRequestCommand;
import com.example.cqrs.domain.LoanApplication.commands.ApproveLoanCommand;
import com.example.cqrs.readmodel.LoanApplicationView;
import com.example.cqrs.readmodel.LoanApplicationViewRepository;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan")
public class LoanApplicationController {

    private final CommandRouter commandRouter;
    private final LoanApplicationViewRepository viewRepository;

    public LoanApplicationController(CommandRouter commandRouter, LoanApplicationViewRepository viewRepository) {
        this.commandRouter = commandRouter;
        this.viewRepository = viewRepository;
    }

    public record ApplyRequest(String applicant, String amount, String currency, String locationType) {}

    @PostMapping
    public String apply(@RequestBody ApplyRequest body) {
        var command = new ApplyLoanRequestCommand(
                body.applicant(),
                body.amount(),
                body.currency() != null ? body.currency() : "EUR",
                body.locationType() != null ? body.locationType() : "POSTAL"
        );
        commandRouter.send(command);
        return command.getApplicationId();
    }

    public record ApproveRequest(String applicationId) {}

    @PostMapping("/approve")
    public void approve(@RequestBody ApproveRequest body) {
        commandRouter.send(new ApproveLoanCommand(body.applicationId()));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<LoanApplicationView> get(@PathVariable String applicationId) {
        return viewRepository.findById(applicationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
