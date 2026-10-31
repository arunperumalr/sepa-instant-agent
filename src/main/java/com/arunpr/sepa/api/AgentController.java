package com.arunpr.sepa.api;

import com.arunpr.sepa.agent.SupportAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    public record AskRequest(String question) { }

    public record AskResponse(String answer) { }

    private final SupportAgentService agent;

    public AgentController(SupportAgentService agent) {
        this.agent = agent;
    }

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@RequestBody AskRequest request) {
        SupportAgentService.AgentReply reply = agent.ask(request.question());
        AskResponse body = new AskResponse(reply.answer());
        return reply.blocked() ? ResponseEntity.badRequest().body(body) : ResponseEntity.ok(body);
    }
}