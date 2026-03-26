package com.airline.feedback.repo;

import com.airline.feedback.model.AirlineCase;
import com.airline.feedback.model.CaseStatus;
import com.airline.feedback.model.CaseType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface AirlineCaseRepository extends JpaRepository<AirlineCase, String> {

  Optional<AirlineCase> findByTicketNumber(String ticketNumber);

  List<AirlineCase> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail, Pageable pageable);

  long countByType(CaseType type);

  long countByStatus(CaseStatus status);

  long countByPriorityGreaterThanEqual(int priority);

  long countByCreatedAtAfter(Instant createdAt);

}

