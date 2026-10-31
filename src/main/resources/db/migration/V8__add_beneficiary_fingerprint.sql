ALTER TABLE sepa_txn
    ADD COLUMN beneficiary_fp CHAR(64) NULL;

CREATE OR REPLACE SQL SECURITY DEFINER VIEW v_txn_owner_check AS
SELECT txn_ref, beneficiary_fp
FROM sepa_txn;

GRANT SELECT ON v_txn_owner_check TO 'agent_ro'@'localhost';