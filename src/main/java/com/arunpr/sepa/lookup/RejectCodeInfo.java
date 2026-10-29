package com.arunpr.sepa.lookup;

public record RejectCodeInfo(
        String code,
        String title,
        String description,
        String commonCause,
        String actionNeeded,
        String actionOwner) {
}