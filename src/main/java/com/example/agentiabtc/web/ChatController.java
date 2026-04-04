package com.example.agentiabtc.web;

import com.example.agentiabtc.agents.AIAgent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {
    private final AIAgent aiAgent;

    public ChatController(AIAgent aiAgent) {

        this.aiAgent = aiAgent;
    }
    @GetMapping(value = "/chat",produces = MediaType.TEXT_PLAIN_VALUE)
    public String chat(@RequestParam(name = "request") String query){
        return aiAgent.askAgent(query);
    }
}
