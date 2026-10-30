package com.arunpr.sepa.agent;

import com.arunpr.sepa.lookup.TxnLookupService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TxnLookupTool {

    private final TxnLookupService lookupService;

    public TxnLookupTool(TxnLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @Tool(description = "Look up one SEPA transaction by its reference, for example SYN-0002. "
            + "Returns the masked wallet, the status of each processing stage, the event history, "
            + "and the explanation of any bank reject code. Use it only for a reference the user typed.")
    public Object lookupTransaction(
            @ToolParam(description = "The transaction reference: letters, digits and hyphens only") String txnRef) {
        try {
            return lookupService.lookup(txnRef)
                    .map(result -> (Object) result)
                    .orElse(Map.of("found", false, "message", "No transaction with that reference"));
        } catch (IllegalArgumentException e) {
            return Map.of("error", "That is not a valid transaction reference format");
        }
    }
}