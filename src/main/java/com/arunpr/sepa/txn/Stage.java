package com.arunpr.sepa.txn;

public enum Stage {
    WALLET_DEBIT, WALLET_CREDIT, BANK_DEBIT, BANK_CREDIT,
    POSTPAY_DEBIT, POSTPAY_CREDIT, AUDIT, RECALL
}