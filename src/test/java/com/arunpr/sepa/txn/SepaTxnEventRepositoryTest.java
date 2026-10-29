package com.arunpr.sepa.txn;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
class SepaTxnEventRepositoryTest {

    @Autowired
    private SepaTxnRepository txnRepository;

    @Autowired
    private SepaTxnEventRepository eventRepository;

    @Autowired
    private EntityManager entityManager;

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private SepaTxn saveNewTxn() {
        SepaTxn txn = new SepaTxn("TST-" + shortId(), UUID.randomUUID().toString(),
                Scheme.INSTANT, "W-" + shortId(), "BEN-" + shortId(), new BigDecimal("25.50"));
        return txnRepository.saveAndFlush(txn);
    }

    @Test
    void savesTwoEventsAndReadsThemBackInOrder() {
        Long txnId = saveNewTxn().getId();

        eventRepository.saveAndFlush(
                new SepaTxnEvent(txnId, Stage.BANK_CREDIT, null, StageStatus.PEND, null));
        eventRepository.saveAndFlush(
                new SepaTxnEvent(txnId, Stage.BANK_CREDIT, StageStatus.PEND, StageStatus.FAILED, "AC01"));
        entityManager.clear();   // force the read to come from MySQL

        List<SepaTxnEvent> events = eventRepository.findByTxnIdOrderByIdAsc(txnId);

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getNewStatus()).isEqualTo(StageStatus.PEND);
        assertThat(events.get(0).getOldStatus()).isNull();
        assertThat(events.get(1).getNewStatus()).isEqualTo(StageStatus.FAILED);
        assertThat(events.get(1).getDetailCode()).isEqualTo("AC01");
        assertThat(events.get(1).getEventTs()).isNotNull();   // MySQL default
    }

    @Test
    void eventForNonExistentTransactionIsRejected() {
        SepaTxnEvent orphan =
                new SepaTxnEvent(999_999_999L, Stage.WALLET_DEBIT, null, StageStatus.SUCC, null);

        assertThatThrownBy(() -> eventRepository.saveAndFlush(orphan))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}