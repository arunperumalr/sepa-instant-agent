package com.arunpr.sepa.api;

import com.arunpr.sepa.lookup.TxnLookupResult;
import com.arunpr.sepa.lookup.TxnLookupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TxnLookupService lookupService;

    public TransactionController(TxnLookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/{txnRef}")
    public ResponseEntity<TxnLookupResult> get(@PathVariable String txnRef) {
        return lookupService.lookup(txnRef)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}