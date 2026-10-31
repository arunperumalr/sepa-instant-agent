package com.arunpr.sepa.guard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiRedactorTest {

    private final PiiRedactor redactor = new PiiRedactor();

    @Test
    void redactsCompactAndSpacedIban() {
        assertThat(redactor.redact("IBAN DE89370400440532013000 failed"))
                .isEqualTo("IBAN [IBAN] failed");
        assertThat(redactor.redact("IBAN DE89 3704 0044 0532 0130 00 failed"))
                .isEqualTo("IBAN [IBAN] failed");
    }

    @Test
    void redactsValidCardButNotRandomDigits() {
        assertThat(redactor.redact("card 4111 1111 1111 1111 declined")).isEqualTo("card [CARD] declined");
        assertThat(redactor.redact("number 4111 1111 1111 1112 here")).isEqualTo("number 4111 1111 1111 1112 here");
    }

    @Test
    void redactsEmail() {
        assertThat(redactor.redact("mail me at john.doe@example.com please"))
                .isEqualTo("mail me at [EMAIL] please");
    }

    @Test
    void leavesTransactionReferencesAlone() {
        String text = "Why was SYN-0002 rejected? Wallet WLT-10000002, amount 75.50";
        assertThat(redactor.redact(text)).isEqualTo(text);
    }

    @Test
    void findsIbanInRawText() {
        assertThat(redactor.findIban("Customer DE89 3704 0044 0532 0130 00 says SYN-0002 failed"))
                .contains("DE89 3704 0044 0532 0130 00");
        assertThat(redactor.findIban("Why was SYN-0002 rejected?")).isEmpty();
    }
}