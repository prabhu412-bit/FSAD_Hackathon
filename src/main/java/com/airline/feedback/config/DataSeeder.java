package com.airline.feedback.config;

import com.airline.feedback.model.AirlineCase;
import com.airline.feedback.model.CaseStatus;
import com.airline.feedback.model.CaseType;
import com.airline.feedback.repo.AirlineCaseRepository;
import com.airline.feedback.service.TicketService;
import com.airline.feedback.service.TriageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.LocalDate;

@Configuration
public class DataSeeder {

  @Bean
  CommandLineRunner seedDemoData(AirlineCaseRepository repository, TicketService ticketService, TriageService triageService) {
    return args -> {
      // Avoid seeding if DB already has data
      if (!repository.findAll().isEmpty()) return;

      create(repository, ticketService, triageService,
          CaseType.FEEDBACK,
          "Priya Nair",
          "priya@example.com",
          "EK200",
          LocalDate.now().minusDays(2),
          "web",
          "Amazing service! Loved the onboard crew and the smooth check-in.",
          CaseStatus.RESOLVED,
          "Agent Lila",
          "Thanks for your kind words. We’ve shared your feedback with our crew."
      );

      create(repository, ticketService, triageService,
          CaseType.COMPLAINT,
          "John Smith",
          "john@example.com",
          "AA123",
          LocalDate.now().minusDays(1),
          "app",
          "My flight was delayed for hours and I missed my connection. Worst experience.",
          CaseStatus.IN_PROGRESS,
          "Agent Omar",
          null
      );

      create(repository, ticketService, triageService,
          CaseType.COMPLAINT,
          "Sara Khan",
          "sara@example.com",
          "BA77",
          LocalDate.now().minusDays(4),
          "call-center",
          "Refund not received yet. I was overcharged and the staff were unhelpful.",
          CaseStatus.TRIAGED,
          null,
          null
      );
    };
  }

  private void create(AirlineCaseRepository repository,
                        TicketService ticketService,
                        TriageService triageService,
                        CaseType type,
                        String customerName,
                        String customerEmail,
                        String flightNumber,
                        LocalDate journeyDate,
                        String contactChannel,
                        String message,
                        CaseStatus status,
                        String assignedAgent,
                        String resolutionText) {
    String ticketNumber = ticketService.nextTicketNumber();
    TriageService.TriageResult triage = triageService.triage(message);
    Instant now = Instant.now();

    AirlineCase c = new AirlineCase(
        ticketNumber,
        type,
        status,
        customerName,
        customerEmail,
        flightNumber,
        journeyDate,
        contactChannel,
        message,
        triage.getCategory(),
        triage.getSentimentScore(),
        triage.getPriority(),
        assignedAgent,
        triage.getResolutionSuggestion(),
        now,
        now
    );

    if (resolutionText != null && !resolutionText.isBlank()) {
      c.addResolution(resolutionText, now);
      c.updateStatus(CaseStatus.RESOLVED, assignedAgent, now);
    }

    repository.save(c);
  }
}

