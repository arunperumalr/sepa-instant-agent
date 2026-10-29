package com.arunpr.sepa.txn;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface SepaTxnRepository extends JpaRepository<SepaTxn, Long> {

    Optional<SepaTxn> findByTxnRef(String txnRef);

    @Query("""
            select t from SepaTxn t
            where t.scheme = :scheme
              and t.direction = com.arunpr.sepa.txn.Direction.WITHDRAWAL
              and t.walletDebitStatus = com.arunpr.sepa.txn.StageStatus.SUCC
              and (t.bankCreditStatus is null or t.bankCreditStatus in :statuses)
              and t.createdAt < :cutoff
            order by t.createdAt
            """)
    Page<SepaTxn> findStuckWithdrawals(@Param("scheme") Scheme scheme,
                                       @Param("cutoff") LocalDateTime cutoff,
                                       @Param("statuses") Collection<StageStatus> statuses,
                                       Pageable pageable);
}