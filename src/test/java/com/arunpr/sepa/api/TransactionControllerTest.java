package com.arunpr.sepa.api;

import com.arunpr.sepa.lookup.TxnLookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TxnLookupService lookupService;

    @Test
    void unknownReferenceReturns404() throws Exception {
        when(lookupService.lookup("NOPE-0000")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/{ref}", "NOPE-0000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidReferenceReturns400() throws Exception {
        when(lookupService.lookup("bad!")).thenThrow(new IllegalArgumentException("Invalid transaction reference"));

        mockMvc.perform(get("/api/transactions/{ref}", "bad!"))
                .andExpect(status().isBadRequest());
    }
}