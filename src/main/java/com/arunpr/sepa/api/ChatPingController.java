package com.arunpr.sepa.api;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatPingController {

    private final ChatClient chatClient;

    public ChatPingController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/api/chat/ping")
    public String ping(@RequestParam(defaultValue = "Say hello in five words.") String message) {
        return chatClient.prompt().user(message).call().content();
    }
}