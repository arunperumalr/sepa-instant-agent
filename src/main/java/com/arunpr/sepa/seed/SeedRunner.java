package com.arunpr.sepa.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@Profile("seed")
public class SeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);

    private final JdbcClient jdbc;

    public SeedRunner(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int inserted = 0;
        int skipped = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/sample_txns.csv").getInputStream(),
                StandardCharsets.UTF_8))) {
            reader.readLine();   // skip the header row
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] fields = line.split(",", -1);   // -1 keeps empty trailing fields
                if (insertIfMissing(fields)) inserted++; else skipped++;
            }
        }
        log.info("Seed finished: {} inserted, {} already present", inserted, skipped);
    }

    private boolean insertIfMissing(String[] f) {
        Integer existing = jdbc.sql("SELECT COUNT(*) FROM sepa_txn WHERE txn_ref = ?")
                .param(f[0])
                .query(Integer.class)
                .single();
        if (existing > 0) {
            return false;
        }

        int ageMinutes = Integer.parseInt(f[11].trim());

        jdbc.sql("""
                INSERT INTO sepa_txn
                  (txn_ref, idempotency_key, scheme, direction, wallet_id, beneficiary_hash, amount,
                   wallet_debit_status, bank_debit_status, bank_credit_status, reject_code,
                   created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                        DATE_SUB(NOW(3), INTERVAL ? MINUTE),
                        DATE_SUB(NOW(3), INTERVAL ? MINUTE))
                """)
                .params(Arrays.asList(f[0], f[1], f[2], f[3], f[4], f[5], new BigDecimal(f[6]),
                        f[7], nullIfBlank(f[8]), nullIfBlank(f[9]), nullIfBlank(f[10]),
                        ageMinutes, ageMinutes))
                .update();
        insertEvents(f, ageMinutes);

        return true;
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private void insertEvents(String[] f, int ageMinutes) {
        Long txnId = jdbc.sql("SELECT txn_id FROM sepa_txn WHERE txn_ref = ?")
                .param(f[0])
                .query(Long.class)
                .single();
        insertStageEvents(txnId, "WALLET_DEBIT", nullIfBlank(f[7]), null, ageMinutes);
        insertStageEvents(txnId, "BANK_DEBIT", nullIfBlank(f[8]), null, ageMinutes);
        insertStageEvents(txnId, "BANK_CREDIT", nullIfBlank(f[9]), nullIfBlank(f[10]), ageMinutes);
    }

    private void insertStageEvents(Long txnId, String stage, String status,
                                   String rejectCode, int ageMinutes) {
        if (status == null) {
            return;   // this stage never started
        }
        insertEvent(txnId, stage, null, "PEND", null, ageMinutes);
        if (!status.equals("PEND")) {
            String detail = status.equals("FAILED") ? rejectCode : null;
            insertEvent(txnId, stage, "PEND", status, detail, ageMinutes);
        }
    }

    private void insertEvent(Long txnId, String stage, String oldStatus, String newStatus,
                             String detailCode, int ageMinutes) {
        jdbc.sql("""
                INSERT INTO sepa_txn_event (txn_id, stage, old_status, new_status, detail_code, event_ts)
                VALUES (?, ?, ?, ?, ?, DATE_SUB(NOW(3), INTERVAL ? MINUTE))
                """)
                .params(Arrays.asList(txnId, stage, oldStatus, newStatus, detailCode, ageMinutes))
                .update();
    }
}