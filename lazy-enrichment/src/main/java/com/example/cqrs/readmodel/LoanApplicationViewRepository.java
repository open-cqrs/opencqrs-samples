package com.example.cqrs.readmodel;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationViewRepository extends JpaRepository<LoanApplicationView, String> {
}
