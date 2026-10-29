package com.arunpr.sepa.lookup;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
class TxnLookupServiceTest {

    @Autowired
    private TxnLookupService service;

    @Test
    void rejectedWithdrawalReturnsRejectInfoAndHistory() {
        TxnLookupResult result = service.lookup("SYN-0002").orElseThrow();

        assertThat(result.status().walletMasked()).isEqualTo("****0002");
        assertThat(result.status().bankCreditStatus()).isEqualTo("FAILED");
        assertThat(result.rejectInfo().code()).isEqualTo("AC01");
        assertThat(result.rejectInfo().actionOwner()).isEqualTo("CUSTOMER");
        assertThat(result.rejectCodeUnknown()).isFalse();
        assertThat(result.events()).hasSize(6);
        assertThat(result.events().get(5).detailCode()).isEqualTo("AC01");
    }

    @Test
    void unknownRejectCodeIsFlagged() {
        TxnLookupResult result = service.lookup("SYN-0010").orElseThrow();

        assertThat(result.status().rejectCode()).isEqualTo("ZZ99");
        assertThat(result.rejectInfo()).isNull();
        assertThat(result.rejectCodeUnknown()).isTrue();
    }

    @Test
    void successfulTransactionHasNoRejectInfo() {
        TxnLookupResult result = service.lookup("SYN-0001").orElseThrow();

        assertThat(result.status().rejectCode()).isNull();
        assertThat(result.rejectInfo()).isNull();
        assertThat(result.rejectCodeUnknown()).isFalse();
    }

    @Test
    void missingTransactionReturnsEmpty() {
        Optional<TxnLookupResult> result = service.lookup("NOPE-0000");
        assertThat(result).isEmpty();
    }

    @Test
    void badReferenceIsRejectedBeforeTheDatabase() {
        assertThatThrownBy(() -> service.lookup("x'; DROP TABLE sepa_txn;--"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}