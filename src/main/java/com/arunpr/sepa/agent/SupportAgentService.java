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

import java.util.UUID;

@Service
public class SupportAgentService {

    public record AgentReply(String answer, boolean blocked) { }

    private static final Logger log = LoggerFactory.getLogger(SupportAgentService.class);

    private final ChatClient chatClient;
    private final InputGuard inputGuard;
    private final OutputGuard outputGuard;

    public SupportAgentService(ChatClient.Builder builder,
                               TxnLookupTool lookupTool,
                               InputGuard inputGuard,
                               OutputGuard outputGuard,
                               @Value("classpath:prompts/support-agent-v2.txt") Resource systemPrompt) {
        this.inputGuard = inputGuard;
        this.outputGuard = outputGuard;
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

        ChatResponse response = chatClient.prompt()
                .user(guarded.text())
                .call()
                .chatResponse();

        String raw = response.getResult().getOutput().getText();
        String answer = outputGuard.sanitize(raw);

        Integer tokens = response.getMetadata().getUsage().getTotalTokens();
        log.info("req={} ok tokens={} ms={} question=\"{}\"",
                requestId, tokens, System.currentTimeMillis() - start, abbreviate(guarded.text(), 120));
        return new AgentReply(answer, false);
    }

    private static String abbreviate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}