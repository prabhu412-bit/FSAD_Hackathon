package com.airline.feedback.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TicketService {
  private final AtomicLong seq = new AtomicLong(1);

  public String nextTicketNumber() {
    String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
    long n = seq.getAndIncrement();
    return "AIR-" + datePart + "-" + String.format("%05d", n);
  }
}

