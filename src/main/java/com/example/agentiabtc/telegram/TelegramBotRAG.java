package com.example.agentiabtc.telegram;

import com.example.agentiabtc.agents.AIAgentRAG;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.GetWebhookInfo;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBotRAG extends TelegramLongPollingBot {


    @Value("${telegram.api.key}")
    private String telegramBotToken;
    @Value("${telegram.bot.username}")
    private String botUsername;

    private AIAgentRAG aiAgent;

    public TelegramBotRAG(AIAgentRAG aiAgent){
        this.aiAgent = aiAgent;
    }

    @PostConstruct
    public void registerTelegramBot() {
        TelegramBotsApi api;
        try {
            api = new TelegramBotsApi(DefaultBotSession.class);

            // Vérifier si un webhook est configuré
            var webhookInfo = execute(new GetWebhookInfo());
            if (webhookInfo.getUrl() != null && !webhookInfo.getUrl().isEmpty()) {
                System.out.println("Removing existing webhook: " + webhookInfo.getUrl());
                this.clearWebhook();
            } else {
                System.out.println("No webhook configured, skipping removal");
            }

            api.registerBot(this);
            System.out.println("Telegram bot registered successfully!");
        } catch (TelegramApiRequestException e) {
            if (e.getErrorCode() == 404) {
                System.out.println("No webhook to remove, continuing...");
            } else {
                System.err.println("Telegram API error: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Unexpected error during Telegram bot registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void sendTextMessage(long chatId, String text) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId),text);
        execute(sendMessage);
    }

    private void sendTypingQuestion(long chatId) throws TelegramApiException {
        SendChatAction sendChatAction = new SendChatAction();
        sendChatAction.setChatId(String.valueOf(chatId));
        sendChatAction.setAction(ActionType.TYPING);
        execute(sendChatAction);
    }
    @Override
    public void onUpdateReceived(Update telegramRequest) {
        try {
            if (!telegramRequest.hasMessage()) return;

            String messageText = telegramRequest.getMessage().getText();
            Long chatId = telegramRequest.getMessage().getChatId();

            // Gestion des médias (photos)
            String caption = telegramRequest.getMessage().getCaption();
            if (caption == null) caption = "What do you see in this image ?";
            List<Media> mediaList = null; // par défaut null

            List<PhotoSize> photos = telegramRequest.getMessage().getPhoto();
            if (photos != null && !photos.isEmpty()) {
                mediaList = new ArrayList<>();
                for (PhotoSize ps : photos) {
                    try {
                        String fileId = ps.getFileId();
                        GetFile getFile = new GetFile(fileId);
                        File file = execute(getFile);
                        String filePath = file.getFilePath();
                        String textUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath;

                        // URI.create remplace new URL(String) pour Java 20+
                        mediaList.add(Media.builder()
                                .id(fileId)
                                .mimeType(MimeTypeUtils.IMAGE_PNG)
                                .data(new UrlResource(URI.create(textUrl)))
                                .build());
                    } catch (Exception e) {
                        // Ne bloque pas le bot si une image échoue
                        System.err.println("Erreur lors de la récupération de l'image : " + e.getMessage());
                    }
                }
            }

            // Construction du message pour l'agent
            String query = (messageText != null) ? messageText : caption;
            UserMessage userMessage = UserMessage.builder()
                    .text(query)
                    .media(mediaList) // null si pas de média
                    .build();

            // Indiquer "typing" dans le chat
            sendTypingQuestion(chatId);

            // Appel à l'agent IA
            String answer = aiAgent.askAgentRAG(new Prompt(userMessage));

            // Envoyer la réponse au chat
            sendTextMessage(chatId, answer);

        } catch (TelegramApiException e) {
            throw new RuntimeException("Erreur Telegram API : " + e.getMessage(), e);
        }
    }
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return telegramBotToken;
    }
}
