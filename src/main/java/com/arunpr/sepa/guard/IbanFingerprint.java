package com.arunpr.sepa.guard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import java.util.Locale;

@Component
public class IbanFingerprint {

    private final SecretKeySpec key;

    public IbanFingerprint(@Value("${agent.ownership.pepper}") String pepper) {
        this.key = new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String of(String iban) {
        String normalized = iban.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        try {
            Mac mac = Mac.getInstance("HmacSHA256"); // Mac is not thread-safe, so create one per call
            mac.init(key);
            return HexFormat.of().formatHex(mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC failed", e);
        }
    }
}