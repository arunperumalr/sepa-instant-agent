package com.arunpr.sepa.guard;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OwnershipChecker {

    public enum Verdict { MATCH, MISMATCH, NOT_PROVIDED, NOT_VERIFIABLE }

    // Only SYN- refs, so a wallet id such as WLT-10000002 in the question is ignored
    private static final Pattern TXN_REF = Pattern.compile("\\bSYN-\\d{4,}\\b");

    private final JdbcClient agentJdbc;
    private final PiiRedactor redactor;
    private final IbanFingerprint fingerprint;

    public OwnershipChecker(@Qualifier("agentJdbcClient") JdbcClient agentJdbc,
                            PiiRedactor redactor,
                            IbanFingerprint fingerprint) {
        this.agentJdbc = agentJdbc;
        this.redactor = redactor;
        this.fingerprint = fingerprint;
    }

    public Verdict check(String rawQuestion) {
        Optional<String> iban = redactor.findIban(rawQuestion);
        if (iban.isEmpty()) {
            return Verdict.NOT_PROVIDED;
        }

        List<String> refs = new ArrayList<>();
        Matcher m = TXN_REF.matcher(rawQuestion);
        while (m.find()) {
            refs.add(m.group());
        }
        if (refs.stream().distinct().count() != 1) {
            return Verdict.NOT_VERIFIABLE;   // no ref, or several: we cannot tell which one to compare
        }

        // The comparison happens inside MySQL. Java only ever sees the one-word verdict.
        Optional<String> result = agentJdbc.sql("""
                SELECT CASE WHEN beneficiary_fp IS NULL THEN 'NONE'
                            WHEN beneficiary_fp = ? THEN 'MATCH'
                            ELSE 'MISMATCH' END
                FROM v_txn_owner_check
                WHERE txn_ref = ?
                """)
                .params(fingerprint.of(iban.get()), refs.get(0))
                .query(String.class)
                .optional();

        return result.map(OwnershipChecker::toVerdict).orElse(Verdict.NOT_VERIFIABLE);
    }

    private static Verdict toVerdict(String s) {
        return switch (s) {
            case "MATCH" -> Verdict.MATCH;
            case "MISMATCH" -> Verdict.MISMATCH;
            default -> Verdict.NOT_VERIFIABLE;
        };
    }
}