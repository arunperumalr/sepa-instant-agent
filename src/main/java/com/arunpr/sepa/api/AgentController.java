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
    public ResponseEntity<?> ask(@RequestBody AskRequest request) {
        String q = request.question();
        if (q == null || q.isBlank() || q.length() > 500) {
            return ResponseEntity.badRequest().body("Question must be 1 to 500 characters");
        }
        return ResponseEntity.ok(new AskResponse(agent.ask(q)));
    }
}