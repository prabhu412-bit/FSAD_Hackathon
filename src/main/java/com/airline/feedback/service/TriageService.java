package com.airline.feedback.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
public class TriageService {

  public static class TriageResult {
    private final String category;
    private final int sentimentScore; // -5..+5
    private final int priority; // 1..5
    private final String resolutionSuggestion;

    public TriageResult(String category, int sentimentScore, int priority, String resolutionSuggestion) {
      this.category = category;
      this.sentimentScore = sentimentScore;
      this.priority = priority;
      this.resolutionSuggestion = resolutionSuggestion;
    }

    public String getCategory() {
      return category;
    }

    public int getSentimentScore() {
      return sentimentScore;
    }

    public int getPriority() {
      return priority;
    }

    public String getResolutionSuggestion() {
      return resolutionSuggestion;
    }
  }

  private static final Map<String, Integer> POSITIVE = Map.ofEntries(
      Map.entry("great", 2),
      Map.entry("awesome", 2),
      Map.entry("excellent", 2),
      Map.entry("amazing", 2),
      Map.entry("love", 2),
      Map.entry("thank", 1),
      Map.entry("thanks", 1),
      Map.entry("delight", 2),
      Map.entry("wonderful", 2),
      Map.entry("good", 1),
      Map.entry("smooth", 1),
      Map.entry("friendly", 1)
  );

  private static final Map<String, Integer> NEGATIVE = Map.ofEntries(
      Map.entry("delay", 2),
      Map.entry("delayed", 2),
      Map.entry("late", 2),
      Map.entry("cancel", 2),
      Map.entry("cancelled", 2),
      Map.entry("cancellation", 2),
      Map.entry("missed", 3),
      Map.entry("connection", 3),
      Map.entry("refund", 3),
      Map.entry("overcharged", 3),
      Map.entry("charged", 2),
      Map.entry("rude", 2),
      Map.entry("angry", 3),
      Map.entry("worst", 3),
      Map.entry("lost", 3),
      Map.entry("baggage", 3),
      Map.entry("luggage", 3),
      Map.entry("damaged", 2),
      Map.entry("broken", 2),
      Map.entry("unhelpful", 2),
      Map.entry("complaint", 2),
      Map.entry("issue", 2),
      Map.entry("problem", 2),
      Map.entry("unsafe", 4),
      Map.entry("overbooked", 3),
      Map.entry("chargeback", 3)
  );

  private static final Set<String> URGENCY_PHRASES = Set.of(
      "hours", "overnight", "missed my connection", "no updates", "still", "refused", "threat", "unsafe"
  );

  private static final Map<String, Integer> CATEGORY_PRIORITY_BONUS = Map.of(
      "Flight Disruption", 2,
      "Baggage/Handling", 2,
      "Billing/Refund", 1,
      "Service Quality", 1,
      "General Feedback", 0
  );

  public TriageResult triage(String message) {
    String m = (message == null ? "" : message).toLowerCase();

    String category = detectCategory(m);
    int sentiment = detectSentiment(m);
    int priority = computePriority(category, sentiment, m);
    String suggestion = buildResolutionSuggestion(category, sentiment, priority);
    return new TriageResult(category, sentiment, priority, suggestion);
  }

  private String detectCategory(String lowerMessage) {
    Function<String, Boolean> has = term -> lowerMessage.contains(term);

    boolean baggage = has.apply("baggage") || has.apply("lost bag") || has.apply("luggage") || has.apply("bag ")
        || has.apply("damaged") || has.apply("broken");
    if (baggage) return "Baggage/Handling";

    boolean disruption = has.apply("delay") || has.apply("delayed") || has.apply("late")
        || has.apply("cancel") || has.apply("cancelled") || has.apply("cancellation");
    if (disruption) return "Flight Disruption";

    boolean billing = has.apply("refund") || has.apply("overcharged") || has.apply("charged") || has.apply("billing")
        || has.apply("money");
    if (billing) return "Billing/Refund";

    boolean service = has.apply("staff") || has.apply("agent") || has.apply("rude") || has.apply("unhelpful")
        || has.apply("customer service") || has.apply("support");
    if (service) return "Service Quality";

    return "General Feedback";
  }

  private int detectSentiment(String lowerMessage) {
    int score = 0;
    for (Map.Entry<String, Integer> p : POSITIVE.entrySet()) {
      if (lowerMessage.contains(p.getKey())) score += p.getValue();
    }
    for (Map.Entry<String, Integer> n : NEGATIVE.entrySet()) {
      if (lowerMessage.contains(n.getKey())) score -= n.getValue();
    }

    // Normalize to roughly -5..+5
    if (score > 5) score = 5;
    if (score < -5) score = -5;
    return score;
  }

  private int computePriority(String category, int sentimentScore, String lowerMessage) {
    // Base priority by category:
    int base;
    switch (category) {
      case "Flight Disruption" -> base = 4;
      case "Baggage/Handling" -> base = 4;
      case "Billing/Refund" -> base = 3;
      case "Service Quality" -> base = 2;
      default -> base = 1;
    }

    int bonus = CATEGORY_PRIORITY_BONUS.getOrDefault(category, 0);

    // Urgency heuristics: delays/cancellations + strong complaint signals.
    int urgency = 0;
    for (String phrase : URGENCY_PHRASES) {
      if (lowerMessage.contains(phrase)) urgency += 1;
    }
    // If user mentions any hours/digits near delay terms, treat as more urgent.
    if ((lowerMessage.contains("delay") || lowerMessage.contains("delayed") || lowerMessage.contains("cancel") || lowerMessage.contains("cancelled")) &&
        lowerMessage.matches(".*\\b\\d+\\b.*")) {
      urgency += 1;
    }

    // Negative sentiment increases priority; positive reduces it.
    int sentimentBoost = sentimentScore < 0 ? Math.min(2, Math.abs(sentimentScore) / 2 + 0) : 0;
    int sentimentRelief = sentimentScore > 0 ? Math.min(2, sentimentScore / 2) : 0;

    int priority = base + bonus + urgency + sentimentBoost - sentimentRelief;
    if (priority < 1) priority = 1;
    if (priority > 5) priority = 5;
    return priority;
  }

  private String buildResolutionSuggestion(String category, int sentimentScore, int priority) {
    String tone = sentimentScore < 0 ? "apology + action plan" : "confirm + appreciation";
    String urgency = priority >= 4 ? "We will escalate immediately." : "We will address within our standard SLA.";

    return switch (category) {
      case "Flight Disruption" ->
          "Action: verify disruption details, check rebooking/connection options, and update the customer with the latest status. "
              + tone + ". " + urgency;
      case "Baggage/Handling" ->
          "Action: start baggage tracing/handling workflow, share the latest scan updates, and offer compensation if applicable. "
              + tone + ". " + urgency;
      case "Billing/Refund" ->
          "Action: review the transaction, confirm refund eligibility, and process refund/credit with an itemized receipt. "
              + tone + ". " + urgency;
      case "Service Quality" ->
          "Action: review staff interaction logs, follow up with the customer, and apply service improvements. "
              + tone + ". " + urgency;
      default ->
          "Action: review feedback, confirm what happened, and share next steps. " + tone + ". " + urgency;
    };
  }
}

