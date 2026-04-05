package com.example.agentiabtc.web;

import com.example.agentiabtc.agents.AIAgentRAG;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatControllerRAG {
    private final AIAgentRAG aiAgent;

    public ChatControllerRAG(AIAgentRAG aiAgent) {

        this.aiAgent = aiAgent;
    }
    @GetMapping(value = "/chat",produces = MediaType.TEXT_PLAIN_VALUE)
    public String chat(@RequestParam(name = "request") String query){
        return aiAgent.askAgentRAG(new Prompt(query));
    }
}
