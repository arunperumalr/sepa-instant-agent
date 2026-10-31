package com.arunpr.sepa.guard;

import com.arunpr.sepa.guard.OwnershipChecker.Verdict;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class OwnershipCheckerTest {

    @Autowired
    OwnershipChecker checker;

    @Test
    void matchWhenIbanBelongsToTransaction() {
        assertThat(checker.check("Customer DE89370400440532013000 says SYN-0002 failed"))
                .isEqualTo(Verdict.MATCH);
    }

    @Test
    void mismatchWhenIbanIsDifferent() {
        assertThat(checker.check("Customer GB82WEST12345698765432 says SYN-0002 failed"))
                .isEqualTo(Verdict.MISMATCH);
    }

    @Test
    void notProvidedWhenNoIban() {
        assertThat(checker.check("Why was SYN-0002 rejected?")).isEqualTo(Verdict.NOT_PROVIDED);
    }

    @Test
    void notVerifiableWhenNoFingerprintStored() {
        assertThat(checker.check("Customer DE89370400440532013000 says SYN-0004 failed"))
                .isEqualTo(Verdict.NOT_VERIFIABLE);
    }

    @Test
    void notVerifiableWhenRefUnknownOrMissing() {
        assertThat(checker.check("IBAN DE89370400440532013000 and SYN-9999")).isEqualTo(Verdict.NOT_VERIFIABLE);
        assertThat(checker.check("IBAN DE89370400440532013000 failed")).isEqualTo(Verdict.NOT_VERIFIABLE);
    }
}