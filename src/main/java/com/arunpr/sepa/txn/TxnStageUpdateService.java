package com.arunpr.sepa.txn;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TxnStageUpdateService {

    private final SepaTxnRepository txnRepository;
    private final SepaTxnEventRepository eventRepository;

    public TxnStageUpdateService(SepaTxnRepository txnRepository,
                                 SepaTxnEventRepository eventRepository) {
        this.txnRepository = txnRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public SepaTxn updateStage(String txnRef, Stage stage, StageStatus newStatus, String detailCode) {
        SepaTxn txn = txnRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new IllegalArgumentException("Unknown transaction: " + txnRef));

        StageStatus old = txn.statusOf(stage);
        if (old == newStatus) {
            return txn;   // same status again: nothing to do, so retries are safe
        }
        if (old == StageStatus.SUCC || old == StageStatus.FAILED) {
            throw new IllegalStateException(
                    stage + " is already " + old + " and cannot change to " + newStatus);
        }

        txn.applyStatus(stage, newStatus);
        if (stage == Stage.BANK_CREDIT && newStatus == StageStatus.FAILED) {
            txn.setRejectCode(detailCode);
        }
        eventRepository.save(new SepaTxnEvent(txn.getId(), stage, old, newStatus, detailCode));
        return txn;
    }
}