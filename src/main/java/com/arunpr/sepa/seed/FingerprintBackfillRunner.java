package com.arunpr.sepa.seed;

import com.arunpr.sepa.guard.IbanFingerprint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Component
@Profile("seed-fp")
public class FingerprintBackfillRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FingerprintBackfillRunner.class);

    // Public documentation-example IBANs, so this is not real customer data
    private static final Map<String, String> DEMO_IBANS = Map.of(
            "SYN-0001", "GB82WEST12345698765432",
            "SYN-0002", "DE89370400440532013000",
            "SYN-0003", "FR1420041010050500013M02606");

    private final JdbcClient jdbc;
    private final IbanFingerprint fingerprint;

    public FingerprintBackfillRunner(DataSource dataSource, IbanFingerprint fingerprint) {
        this.jdbc = JdbcClient.create(dataSource);
        this.fingerprint = fingerprint;
    }

    @Override
    public void run(String... args) {
        DEMO_IBANS.forEach((txnRef, iban) -> {
            int rows = jdbc.sql("UPDATE sepa_txn SET beneficiary_fp = ? WHERE txn_ref = ?")
                    .params(fingerprint.of(iban), txnRef)
                    .update();
            log.info("fingerprint set txnRef={} rows={}", txnRef, rows);
        });
    }
}