package com.example.agentiabtc.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AIAgentRAG {

    private final ChatClient chatClient;

    public AIAgentRAG(ChatClient.Builder builder,
                      ChatMemory memory,
                      ToolCallbackProvider toolCallbackProvider
    ) {
        Arrays.stream(toolCallbackProvider.getToolCallbacks()).forEach(toolCallback -> {
            System.out.println("-------------------------------------------");
            System.out.println(toolCallback.getToolDefinition());
            System.out.println("-------------------------------------------");
        });
        this.chatClient = builder
                .defaultSystem("""
                 Vous êtes un assistant professionnel.

                 Vous avez accès à des outils pour répondre aux questions.

                 RÈGLES IMPORTANTES :
                - Utilisez les outils si nécessaire
                 - NE mentionnez JAMAIS les outils utilisés
                 - NE dites PAS comment vous avez obtenu l'information
                - Répondez directement avec les informations finales

                FORMAT DE RÉPONSE :
               Quand vous donnez des informations sur un employé :

               Voici les informations :
               - Nom :
               - Salaire :
               - Ancienneté :

               Ne rajoutez aucune explication.
               """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }

    public String askAgentRAG(Prompt prompt){
        return chatClient.prompt(prompt).call().content();
    }
}
