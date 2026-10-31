package com.arunpr.sepa.agent;

import com.arunpr.sepa.guard.InputGuard;
import com.arunpr.sepa.guard.OutputGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.arunpr.sepa.guard.OwnershipChecker;

import java.util.UUID;

@Service
public class SupportAgentService {

    private final OwnershipChecker ownershipChecker;

    public record AgentReply(String answer, boolean blocked) { }

    private static final Logger log = LoggerFactory.getLogger(SupportAgentService.class);

    private final ChatClient chatClient;
    private final InputGuard inputGuard;
    private final OutputGuard outputGuard;

    private static final String MISMATCH_REPLY =
            "The IBAN you provided does not match this transaction, so I can't share its details. "
                    + "Please re-verify the customer and the IBAN, and escalate if the mismatch continues.";

    public SupportAgentService(ChatClient.Builder builder,
                               TxnLookupTool lookupTool,
                               InputGuard inputGuard,
                               OutputGuard outputGuard,
                               OwnershipChecker ownershipChecker,
                               @Value("classpath:prompts/support-agent-v3.txt") Resource systemPrompt) {
        this.inputGuard = inputGuard;
        this.outputGuard = outputGuard;
        this.ownershipChecker = ownershipChecker;
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .defaultTools(lookupTool)
                .build();
    }

    public AgentReply ask(String question) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long start = System.currentTimeMillis();

        InputGuard.GuardResult guarded = inputGuard.check(question);
        if (!guarded.allowed()) {
            log.warn("req={} blocked reason={}", requestId, guarded.reason());
            return new AgentReply("I can't process that request.", true);
        }

        // Uses the raw question: the IBAN is fingerprinted here and never reaches the model
        OwnershipChecker.Verdict ownership = ownershipChecker.check(question);
        if (ownership == OwnershipChecker.Verdict.MISMATCH) {
            log.info("req={} ownership=MISMATCH model not called", requestId);
            return new AgentReply(MISMATCH_REPLY, false);
        }

        String userMessage = guarded.text() + "\n\nownership_check: " + ownership;

        ChatResponse response = chatClient.prompt()
                .user(userMessage)
                .call()
                .chatResponse();

        String raw = response.getResult().getOutput().getText();
        String answer = outputGuard.sanitize(raw);

        Integer tokens = response.getMetadata().getUsage().getTotalTokens();
        log.info("req={} ok ownership={} tokens={} ms={} question=\"{}\"",
                requestId, ownership, tokens, System.currentTimeMillis() - start,
                abbreviate(guarded.text(), 120));
        String notice = switch (ownership) {
            case MATCH -> "IBAN check: the IBAN matches this transaction.\n\n";
            case NOT_VERIFIABLE -> "IBAN check: the IBAN could not be verified against this transaction.\n\n";
            default -> "";   // NOT_PROVIDED (MISMATCH never reaches here)
        };
        return new AgentReply(notice + answer, false);
    }

    private static String abbreviate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}