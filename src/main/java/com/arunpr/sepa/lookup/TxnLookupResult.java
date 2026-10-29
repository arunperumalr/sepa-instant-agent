package com.arunpr.sepa.lookup;

import java.util.List;

public record TxnLookupResult(
        TxnStatusView status,
        List<TxnEventView> events,
        RejectCodeInfo rejectInfo,        // null when there is no reject code, or the code is unknown
        boolean rejectCodeUnknown) {      // true when the code is not in reject_code_ref
}