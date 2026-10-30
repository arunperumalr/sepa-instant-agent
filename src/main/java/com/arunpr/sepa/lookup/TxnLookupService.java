package com.arunpr.sepa.lookup;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TxnLookupService {

    private final JdbcClient jdbc;

    public TxnLookupService(@Qualifier("agentJdbcClient") JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<TxnLookupResult> lookup(String txnRef) {
        if (txnRef == null || !txnRef.matches("[A-Za-z0-9-]{1,20}")) {
            throw new IllegalArgumentException("Invalid transaction reference");
        }

        Optional<TxnStatusView> status = findStatus(txnRef);
        if (status.isEmpty()) {
            return Optional.empty();
        }

        List<TxnEventView> events = findEvents(txnRef);

        String code = status.get().rejectCode();
        RejectCodeInfo info = null;
        boolean unknown = false;
        if (code != null) {
            info = findRejectInfo(code).orElse(null);
            unknown = (info == null);
        }
        return Optional.of(new TxnLookupResult(status.get(), events, info, unknown));
    }

    private Optional<TxnStatusView> findStatus(String txnRef) {
        return jdbc.sql("""
                SELECT txn_ref, scheme, direction, wallet_masked, amount, currency,
                       wallet_debit_status, wallet_credit_status, bank_debit_status, bank_credit_status,
                       postpay_debit_status, postpay_credit_status, audit_status, last_publish_error,
                       reject_code, recall_status, recall_reason, created_at, updated_at, 
                       TIMESTAMPDIFF(MINUTE, created_at, NOW(3)) AS age_minutes
                FROM v_txn_status WHERE txn_ref = ?
                """)
                .param(txnRef)
                .query((rs, n) -> new TxnStatusView(
                        rs.getString("txn_ref"),
                        rs.getString("scheme"),
                        rs.getString("direction"),
                        rs.getString("wallet_masked"),
                        rs.getObject("amount", BigDecimal.class),
                        rs.getString("currency"),
                        rs.getString("wallet_debit_status"),
                        rs.getString("wallet_credit_status"),
                        rs.getString("bank_debit_status"),
                        rs.getString("bank_credit_status"),
                        rs.getString("postpay_debit_status"),
                        rs.getString("postpay_credit_status"),
                        rs.getString("audit_status"),
                        rs.getString("last_publish_error"),
                        rs.getString("reject_code"),
                        rs.getString("recall_status"),
                        rs.getString("recall_reason"),
                        rs.getObject("created_at", LocalDateTime.class),
                        rs.getObject("updated_at", LocalDateTime.class),
                        rs.getLong("age_minutes"),
                        formatAge(rs.getLong("age_minutes"))))
                .optional();
    }

    private List<TxnEventView> findEvents(String txnRef) {
        return jdbc.sql("""
                SELECT stage, old_status, new_status, detail_code, event_ts
                FROM v_txn_event WHERE txn_ref = ? ORDER BY event_seq
                """)
                .param(txnRef)
                .query((rs, n) -> new TxnEventView(
                        rs.getString("stage"),
                        rs.getString("old_status"),
                        rs.getString("new_status"),
                        rs.getString("detail_code"),
                        rs.getObject("event_ts", LocalDateTime.class)))
                .list();
    }

    private Optional<RejectCodeInfo> findRejectInfo(String code) {
        return jdbc.sql("""
                SELECT code, title, description, common_cause, action_needed, action_owner
                FROM reject_code_ref WHERE code = ?
                """)
                .param(code)
                .query((rs, n) -> new RejectCodeInfo(
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("common_cause"),
                        rs.getString("action_needed"),
                        rs.getString("action_owner")))
                .optional();
    }

    private static String formatAge(long minutes) {
        if (minutes < 60) {
            return minutes + " minutes";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hours " + (minutes % 60) + " minutes";
        }
        return (hours / 24) + " days " + (hours % 24) + " hours";
    }
}