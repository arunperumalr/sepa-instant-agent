CREATE
OR REPLACE SQL SECURITY DEFINER VIEW v_txn_status AS
SELECT txn_ref,
       scheme,
       direction,
       CONCAT('****', RIGHT(wallet_id, 4)) AS wallet_masked,
       amount,
       currency,
       wallet_debit_status,
       wallet_credit_status,
       bank_debit_status,
       bank_credit_status,
       postpay_debit_status,
       postpay_credit_status,
       audit_status,
       last_publish_error,
       reject_code,
       recall_status,
       recall_requested_at,
       recall_reason,
       recall_updated_at,
       created_at,
       updated_at
FROM sepa_txn;

CREATE
OR REPLACE SQL SECURITY DEFINER VIEW v_txn_event AS
SELECT t.txn_ref,
       e.stage,
       e.old_status,
       e.new_status,
       e.detail_code,
       e.event_ts
FROM sepa_txn_event e
         JOIN sepa_txn t ON t.txn_id = e.txn_id;