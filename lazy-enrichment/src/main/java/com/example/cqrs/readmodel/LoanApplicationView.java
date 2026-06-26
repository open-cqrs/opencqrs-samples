package com.example.cqrs.readmodel;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class LoanApplicationView {

    @Id
    private String applicationId;
    private String applicant;
    private String amount;
    private String currency;
    private String locationType;
    private boolean verifiedAddress;
    private String manualReviewResult;
    private boolean approved;

    protected LoanApplicationView() {}

    public LoanApplicationView(String applicationId, String applicant, String amount,
                               String currency, String locationType, boolean verifiedAddress) {
        this.applicationId = applicationId;
        this.applicant = applicant;
        this.amount = amount;
        this.currency = currency;
        this.locationType = locationType;
        this.verifiedAddress = verifiedAddress;
    }

    public String getApplicationId() { return applicationId; }
    public String getApplicant() { return applicant; }
    public String getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getLocationType() { return locationType; }
    public boolean isVerifiedAddress() { return verifiedAddress; }
    public String getManualReviewResult() { return manualReviewResult; }
    public void setManualReviewResult(String manualReviewResult) { this.manualReviewResult = manualReviewResult; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}
