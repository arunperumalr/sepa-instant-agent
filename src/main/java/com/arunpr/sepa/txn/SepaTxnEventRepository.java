package com.arunpr.sepa.txn;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SepaTxnEventRepository extends JpaRepository<SepaTxnEvent, Long> {

    List<SepaTxnEvent> findByTxnIdOrderByIdAsc(Long txnId);
}