CREATE TABLE sepa_txn_event
(
    event_id    BIGINT      NOT NULL AUTO_INCREMENT,
    txn_id      BIGINT      NOT NULL,
    stage       VARCHAR(20) NOT NULL,
    old_status  VARCHAR(10) NULL,
    new_status  VARCHAR(10) NOT NULL,
    detail_code VARCHAR(40) NULL,
    event_ts    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (event_id),

    CONSTRAINT fk_event_txn FOREIGN KEY (txn_id) REFERENCES sepa_txn (txn_id),

    CONSTRAINT ck_event_stage CHECK (stage IN (
                                               'WALLET_DEBIT', 'WALLET_CREDIT', 'BANK_DEBIT', 'BANK_CREDIT',
                                               'POSTPAY_DEBIT', 'POSTPAY_CREDIT', 'AUDIT', 'RECALL')),
    CONSTRAINT ck_event_old_status CHECK (old_status IN ('PEND', 'SUCC', 'FAILED', 'UNKNOWN')),
    CONSTRAINT ck_event_new_status CHECK (new_status IN ('PEND', 'SUCC', 'FAILED', 'UNKNOWN')),

    INDEX       idx_event_txn_ts (txn_id, event_ts)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;