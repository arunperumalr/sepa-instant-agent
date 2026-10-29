package com.arunpr.sepa.txn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional   // each test rolls back, so no data stays behind
class TxnStageUpdateServiceTest {

    @Autowired
    private TxnStageUpdateService service;

    @Autowired
    private SepaTxnRepository txnRepository;

    @Autowired
    private SepaTxnEventRepository eventRepository;

    private SepaTxn newTxn() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return txnRepository.saveAndFlush(new SepaTxn("TST-" + id, UUID.randomUUID().toString(),
                Scheme.INSTANT, Direction.WITHDRAWAL, "W-" + id, "BEN-" + id, new BigDecimal("40.00")));
    }

    @Test
    void bankCreditFailureStoresRejectCodeAndEvent() {
        SepaTxn txn = newTxn();

        service.updateStage(txn.getTxnRef(), Stage.BANK_CREDIT, StageStatus.FAILED, "AC01");

        assertThat(txn.getBankCreditStatus()).isEqualTo(StageStatus.FAILED);
        assertThat(txn.getRejectCode()).isEqualTo("AC01");
        List<SepaTxnEvent> events = eventRepository.findByTxnIdOrderByIdAsc(txn.getId());
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getOldStatus()).isNull();
        assertThat(events.get(0).getNewStatus()).isEqualTo(StageStatus.FAILED);
        assertThat(events.get(0).getDetailCode()).isEqualTo("AC01");
    }

    @Test
    void sameStatusTwiceCreatesOnlyOneEvent() {
        SepaTxn txn = newTxn();

        service.updateStage(txn.getTxnRef(), Stage.BANK_DEBIT, StageStatus.PEND, null);
        service.updateStage(txn.getTxnRef(), Stage.BANK_DEBIT, StageStatus.PEND, null);

        assertThat(eventRepository.findByTxnIdOrderByIdAsc(txn.getId())).hasSize(1);
    }

    @Test
    void finalStatusCannotChange() {
        SepaTxn txn = newTxn();
        service.updateStage(txn.getTxnRef(), Stage.BANK_CREDIT, StageStatus.SUCC, null);

        assertThatThrownBy(() ->
                service.updateStage(txn.getTxnRef(), Stage.BANK_CREDIT, StageStatus.FAILED, "AC01"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unknownTransactionIsRejected() {
        assertThatThrownBy(() ->
                service.updateStage("NOPE-0000", Stage.AUDIT, StageStatus.SUCC, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}