package com.example.donutauction.chat;

/** Result of one matched parsing rule. user/amount/durationMs may be null if the rule has no such group. */
public record ParsedEvent(EventType type, String user, Double amount, Long durationMs,
                          String rawText, long timestampMs, String ruleName) {}
