package com.arunpr.sepa.txn;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
class SepaTxnRepositoryTest {

    @Autowired
    private SepaTxnRepository repository;

    @Autowired
    private EntityManager entityManager;

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private SepaTxn newTxn(String txnRef, String idempotencyKey) {
        return new SepaTxn(txnRef, idempotencyKey, Scheme.INSTANT, Direction.WITHDRAWAL,
                "W-" + shortId(), "BEN-" + shortId(), new BigDecimal("25.50"));
    }

    @Test
    void savesAndReadsBackWithDatabaseDefaults() {
        String ref = "TST-" + shortId();
        repository.saveAndFlush(newTxn(ref, UUID.randomUUID().toString()));
        entityManager.clear();   // force the next read to hit MySQL, not Hibernate's memory

        SepaTxn found = repository.findByTxnRef(ref).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getCreatedAt()).isNotNull();            // MySQL default
        assertThat(found.getWalletDebitStatus()).isEqualTo(StageStatus.PEND);
        assertThat(found.getBankCreditStatus()).isNull();
        assertThat(found.getCurrency()).isEqualTo("EUR");
        assertThat(found.getAmount()).isEqualByComparingTo("25.50");
        assertThat(found.getVersion()).isEqualTo(0);
    }

    @Test
    void updateIncrementsVersion() {
        String ref = "TST-" + shortId();
        repository.saveAndFlush(newTxn(ref, UUID.randomUUID().toString()));
        entityManager.clear();

        SepaTxn txn = repository.findByTxnRef(ref).orElseThrow();
        txn.setBankCreditStatus(StageStatus.SUCC);
        repository.saveAndFlush(txn);

        assertThat(txn.getVersion()).isEqualTo(1);
        entityManager.clear();
        assertThat(repository.findByTxnRef(ref).orElseThrow().getBankCreditStatus())
                .isEqualTo(StageStatus.SUCC);
    }

    @Test
    void duplicateIdempotencyKeyIsRejected() {
        String sameKey = UUID.randomUUID().toString();
        repository.saveAndFlush(newTxn("TST-" + shortId(), sameKey));

        SepaTxn duplicate = newTxn("TST-" + shortId(), sameKey);

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void storesRejectCodeForRejectedWithdrawal() {
        String ref = "TST-" + shortId();
        SepaTxn txn = newTxn(ref, UUID.randomUUID().toString());
        txn.setBankCreditStatus(StageStatus.FAILED);
        txn.setRejectCode("AC01");
        repository.saveAndFlush(txn);
        entityManager.clear();

        SepaTxn found = repository.findByTxnRef(ref).orElseThrow();

        assertThat(found.getDirection()).isEqualTo(Direction.WITHDRAWAL);
        assertThat(found.getBankCreditStatus()).isEqualTo(StageStatus.FAILED);
        assertThat(found.getRejectCode()).isEqualTo("AC01");
    }

    @Test
    void findsWithdrawalsStuckPastCutoff() {
        Page<SepaTxn> page = repository.findStuckWithdrawals(
                Scheme.INSTANT,
                LocalDateTime.now().minusMinutes(30),
                List.of(StageStatus.PEND, StageStatus.UNKNOWN),
                PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(SepaTxn::getTxnRef)
                .contains("SYN-0004", "SYN-0008")
                .doesNotContain("SYN-0001", "SYN-0002", "SYN-0005", "SYN-0011");
    }

    @Test
    void concurrentChangeIsDetectedByVersion() {
        String ref = "TST-" + shortId();
        repository.saveAndFlush(newTxn(ref, UUID.randomUUID().toString()));
        entityManager.clear();

        SepaTxn mine = repository.findByTxnRef(ref).orElseThrow();   // I loaded version 0

        // simulate another user changing the same row meanwhile
        entityManager.createNativeQuery("UPDATE sepa_txn SET version = version + 1 WHERE txn_id = ?1")
                .setParameter(1, mine.getId())
                .executeUpdate();

        mine.setBankCreditStatus(StageStatus.SUCC);

        assertThatThrownBy(() -> repository.saveAndFlush(mine))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}