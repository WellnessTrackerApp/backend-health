package pl.edu.healthapp.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.healthapp.component.PromptBuilder;


@Service
public class GeminiService implements AIService {
    @Value("${gemini.api-model}")
    private String model;

    private final PromptBuilder promptBuilder;

    private final Client client;

    public GeminiService(PromptBuilder promptBuilder, @Value("${gemini.api-key}") String apiKey){
        this.promptBuilder = promptBuilder;
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
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
        GenerateContentResponse response = client.models
                .generateContent(model, prompt, null);
        return response.text();
    }


}
