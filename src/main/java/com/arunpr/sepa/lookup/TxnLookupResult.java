package com.arunpr.sepa.lookup;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TxnLookupResult(
        TxnStatusView status,
        List<TxnEventView> events,
        RejectCodeInfo rejectInfo,
        boolean rejectCodeUnknown) {
}