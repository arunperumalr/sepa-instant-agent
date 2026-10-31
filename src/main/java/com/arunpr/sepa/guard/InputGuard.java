package com.arunpr.sepa.guard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InputGuard {

    public record GuardResult(boolean allowed, String text, String reason) {
        static GuardResult blocked(String reason) {
            return new GuardResult(false, null, reason);
        }
    }

    private static final int MAX_LENGTH = 500;

    // A cheap first layer only. The real protection is that the tools cannot return secrets.
    private static final List<String> INJECTION_PHRASES = List.of(
            "ignore previous instructions", "ignore all previous", "ignore your rules",
            "ignore the rules", "disregard your instructions", "reveal your system prompt",
            "show your system prompt", "forget your instructions");

    private final PiiRedactor redactor;
    private final boolean enabled;

    public InputGuard(PiiRedactor redactor,
                      @Value("${agent.guards.enabled:true}") boolean enabled) {
        this.redactor = redactor;
        this.enabled = enabled;
    }

    public GuardResult check(String question) {
        if (question == null || question.isBlank()) {
            return GuardResult.blocked("empty question");
        }
        if (!enabled) {
            return new GuardResult(true, question, null);
        }
        if (question.length() > MAX_LENGTH) {
            return GuardResult.blocked("question too long");
        }
        String lower = question.toLowerCase();
        for (String phrase : INJECTION_PHRASES) {
            if (lower.contains(phrase)) {
                return GuardResult.blocked("possible prompt injection");
            }
        }
        return new GuardResult(true, redactor.redact(question), null);
    }
}