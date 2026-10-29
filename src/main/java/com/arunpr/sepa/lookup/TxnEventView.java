package com.arunpr.sepa.lookup;

import java.time.LocalDateTime;

public record TxnEventView(
        String stage,
        String oldStatus,
        String newStatus,
        String detailCode,
        LocalDateTime eventTs) {
}