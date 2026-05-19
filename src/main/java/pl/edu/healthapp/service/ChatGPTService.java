package pl.edu.healthapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.edu.healthapp.component.PromptBuilder;
import pl.edu.healthapp.dto.request.ChatGPTRequest;
import pl.edu.healthapp.dto.response.ChatGPTResponse;

import java.time.OffsetDateTime;
import java.util.List;


public class ChatGPTService implements AIService {
    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.api-model}")
    private String model;

    private final RestClient restClient;
    private final PromptBuilder promptBuilder;

    public ChatGPTService(RestClient restClient, PromptBuilder promptBuilder){
        this.restClient = restClient;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public String generateWeeklyAdvice(String username, String language) {
        String prompt = promptBuilder.advicePrompt(username, language);
        return getChatResponse(prompt);
    }

    @Override
    public String answerHealthQuestion(String username, String question) {
        String prompt = promptBuilder.questionPrompt(question);
        return getChatResponse(prompt);
    }

    @Override
    public String generateHealthPrediction(String username, String language) {
        String prompt = promptBuilder.predictionPrompt(username, language);
        return getChatResponse(prompt);
    }

    private String getChatResponse(String prompt){
        ChatGPTRequest chatGPTRequest = new ChatGPTRequest(
                model,
                List.of(new ChatGPTRequest.ChatMessage("user", prompt))
        );

        ChatGPTResponse response = restClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(chatGPTRequest)
                .retrieve()
                .body(ChatGPTResponse.class);

        return response.choices().get(0).message().content();
    }


}
