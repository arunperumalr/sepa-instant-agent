package com.arunpr.sepa.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class SupportAgentService {

    private final ChatClient chatClient;

    public SupportAgentService(ChatClient.Builder builder,
                               TxnLookupTool lookupTool,
                               @Value("classpath:prompts/support-agent-v2.txt") Resource systemPrompt) {
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .defaultTools(lookupTool)
                .build();
    }

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}