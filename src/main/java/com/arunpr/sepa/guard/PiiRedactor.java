package com.arunpr.sepa.guard;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PiiRedactor {

    private static final Pattern IBAN =
            Pattern.compile("\\b[A-Z]{2}\\d{2}(?:\\s?[A-Z0-9]{4}){3,7}(?:\\s?[A-Z0-9]{1,4})?\\b");

    private static final Pattern EMAIL =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    private static final Pattern CARD_CANDIDATE =
            Pattern.compile("\\b(?:\\d[ -]?){12,18}\\d\\b");

    public String redact(String text) {
        if (text == null) {
            return null;
        }
        String result = IBAN.matcher(text).replaceAll("[IBAN]");
        result = EMAIL.matcher(result).replaceAll("[EMAIL]");
        return redactCards(result);
    }

    private String redactCards(String text) {
        Matcher m = CARD_CANDIDATE.matcher(text);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String digits = m.group().replaceAll("[^0-9]", "");
            String replacement = passesLuhn(digits) ? "[CARD]" : m.group();
            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(out);
        return out.toString();
    }

    // Luhn check: real card numbers pass it, random digit strings usually do not
    private static boolean passesLuhn(String digits) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }

    public Optional<String> findIban(String text) {
        if (text == null) {
            return Optional.empty();
        }
        Matcher m = IBAN.matcher(text);
        return m.find() ? Optional.of(m.group()) : Optional.empty();
    }
}