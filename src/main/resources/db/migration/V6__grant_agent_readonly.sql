GRANT
SELECT
ON payments.v_txn_status TO 'agent_ro'@'localhost';
GRANT
SELECT
ON payments.v_txn_event TO 'agent_ro'@'localhost';
GRANT
SELECT
ON payments.reject_code_ref TO 'agent_ro'@'localhost';