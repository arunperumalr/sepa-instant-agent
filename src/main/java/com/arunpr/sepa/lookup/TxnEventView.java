package com.arunpr.sepa.lookup;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TxnEventView(
        String stage,
        String oldStatus,
        String newStatus,
        String detailCode,
        LocalDateTime eventTs) {
}