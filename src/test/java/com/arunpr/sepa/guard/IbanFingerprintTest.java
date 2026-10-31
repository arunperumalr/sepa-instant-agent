package com.arunpr.sepa.guard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IbanFingerprintTest {

    private final IbanFingerprint fp = new IbanFingerprint("test-pepper-1234567890");

    @Test
    void sameIbanGivesSameFingerprintRegardlessOfSpacingAndCase() {
        assertThat(fp.of("DE89 3704 0044 0532 0130 00"))
                .isEqualTo(fp.of("de89370400440532013000"))
                .hasSize(64);
    }

    @Test
    void differentIbanGivesDifferentFingerprint() {
        assertThat(fp.of("DE89370400440532013000"))
                .isNotEqualTo(fp.of("GB82WEST12345698765432"));
    }

    @Test
    void differentPepperGivesDifferentFingerprint() {
        assertThat(new IbanFingerprint("another-pepper-0987654321").of("DE89370400440532013000"))
                .isNotEqualTo(fp.of("DE89370400440532013000"));
    }
}