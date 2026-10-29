CREATE TABLE sepa_txn
(
    txn_id                BIGINT         NOT NULL AUTO_INCREMENT,
    txn_ref               VARCHAR(20)    NOT NULL,
    idempotency_key       VARCHAR(64)    NOT NULL,
    scheme                VARCHAR(10)    NOT NULL,
    wallet_id             VARCHAR(32)    NOT NULL,
    beneficiary_hash      VARCHAR(16)    NOT NULL,
    amount                DECIMAL(18, 2) NOT NULL,
    currency              CHAR(3)        NOT NULL DEFAULT 'EUR',

    wallet_debit_status   VARCHAR(10)    NOT NULL DEFAULT 'PEND',
    wallet_credit_status  VARCHAR(10) NULL,
    bank_debit_status     VARCHAR(10) NULL,
    bank_credit_status    VARCHAR(10) NULL,
    postpay_debit_status  VARCHAR(10) NULL,
    postpay_credit_status VARCHAR(10) NULL,
    audit_status          VARCHAR(10) NULL,
    last_publish_error    VARCHAR(100) NULL,

    recall_status         VARCHAR(10) NULL,
    recall_requested_at   DATETIME(3) NULL,
    recall_reason         VARCHAR(40) NULL,
    recall_updated_at     DATETIME(3) NULL,

    created_at            DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at            DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                        ON UPDATE CURRENT_TIMESTAMP(3),
    version               INT           NOT NULL DEFAULT 0,

    PRIMARY KEY (txn_id),
    CONSTRAINT uq_sepa_txn_ref UNIQUE (txn_ref),
    CONSTRAINT uq_sepa_txn_idem UNIQUE (idempotency_key),

    CONSTRAINT ck_sepa_scheme CHECK (scheme IN ('INSTANT', 'CLASSIC')),
    CONSTRAINT ck_wallet_debit CHECK (wallet_debit_status IN ('PEND', 'SUCC', 'FAILED', 'UNKNOWN')),
    CONSTRAINT ck_wallet_credit CHECK (wallet_credit_status IN ('PEND', 'SUCC', 'FAILED', 'UNKNOWN')),
    CONSTRAINT ck_bank_debit CHECK (bank_debit_status IN ('PEND', 'SUCC', 'FAILED', 'UNKNOWN')),
    CONSTRAINT ck_bank_credit CHECK (bank_credit_status IN ('PEND', 'SUCC', 'FAILED', 'UNKNOWN')),
    CONSTRAINT ck_postpay_debit CHECK (postpay_debit_status IN ('PEND', 'SUCC', 'FAILED', 'UNKNOWN')),
    CONSTRAINT ck_postpay_credit CHECK (postpay_credit_status IN ('PEND', 'SUCC', 'FAILED', 'UNKNOWN')),
    CONSTRAINT ck_audit CHECK (audit_status IN ('PEND', 'SUCC', 'FAILED', 'UNKNOWN')),
    CONSTRAINT ck_recall CHECK (recall_status IN ('PEND', 'SUCC', 'FAILED', 'UNKNOWN')),

    INDEX                 idx_sepa_scheme_created (scheme, created_at),
    INDEX                 idx_sepa_wallet_created (wallet_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;