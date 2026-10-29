CREATE
OR REPLACE SQL SECURITY DEFINER VIEW v_txn_event AS
SELECT t.txn_ref,
       e.event_id AS event_seq,
       e.stage,
       e.old_status,
       e.new_status,
       e.detail_code,
       e.event_ts
FROM sepa_txn_event e
         JOIN sepa_txn t ON t.txn_id = e.txn_id;