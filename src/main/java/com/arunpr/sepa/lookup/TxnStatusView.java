package com.arunpr.sepa.lookup;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TxnStatusView(
        String txnRef,
        String scheme,
        String direction,
        String walletMasked,
        BigDecimal amount,
        String currency,
        String walletDebitStatus,
        String walletCreditStatus,
        String bankDebitStatus,
        String bankCreditStatus,
        String postpayDebitStatus,
        String postpayCreditStatus,
        String auditStatus,
        String lastPublishError,
        String rejectCode,
        String recallStatus,
        String recallReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long ageMinutes,
        String createdAgo) {
}