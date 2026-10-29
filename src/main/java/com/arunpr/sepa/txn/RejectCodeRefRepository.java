package com.arunpr.sepa.txn;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectCodeRefRepository extends JpaRepository<RejectCodeRef, String> {
}