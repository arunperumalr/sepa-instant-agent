package com.arunpr.sepa.lookup;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RejectCodeInfo(
        String code,
        String title,
        String description,
        String commonCause,
        String actionNeeded,
        String actionOwner) {
}