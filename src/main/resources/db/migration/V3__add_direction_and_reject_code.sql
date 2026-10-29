ALTER TABLE sepa_txn
    ADD COLUMN direction VARCHAR(12) NOT NULL,
    ADD COLUMN reject_code VARCHAR(10) NULL,
    ADD CONSTRAINT ck_sepa_direction CHECK (direction IN ('WITHDRAWAL', 'TOPUP'));