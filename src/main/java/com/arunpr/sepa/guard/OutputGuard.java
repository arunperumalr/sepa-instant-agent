package com.arunpr.sepa.guard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OutputGuard {

    private static final int MAX_LENGTH = 2000;
    private static final String FALLBACK = "I could not produce an answer. Please try again or escalate.";

    private final PiiRedactor redactor;
    private final boolean enabled;

    public OutputGuard(PiiRedactor redactor,
                       @Value("${agent.guards.enabled:true}") boolean enabled) {
        this.redactor = redactor;
        this.enabled = enabled;
    }

    public String sanitize(String answer) {
        if (answer == null || answer.isBlank()) {
            return FALLBACK;
        }
        if (!enabled) {
            return answer;
        }
        String clean = redactor.redact(answer);
        return clean.length() > MAX_LENGTH ? clean.substring(0, MAX_LENGTH) : clean;
    }
}