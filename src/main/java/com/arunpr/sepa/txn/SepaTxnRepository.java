package com.arunpr.sepa.txn;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SepaTxnRepository extends JpaRepository<SepaTxn, Long> {

    Optional<SepaTxn> findByTxnRef(String txnRef);
}