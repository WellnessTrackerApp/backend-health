package pl.edu.healthapp.service;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.edu.healthapp.component.PromptBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private Client client;

    @Mock
    private Models models;

    @Mock
    private GenerateContentResponse generateContentResponse;

    private GeminiService geminiService;
    private final String testModel = "gemini-2.5-flash";

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService(promptBuilder, "fake-api-key");
        ReflectionTestUtils.setField(client, "models", models);
        ReflectionTestUtils.setField(geminiService, "model", testModel);
        ReflectionTestUtils.setField(geminiService, "client", client);
    }

    private void mockGeminiResponse(String expectedPrompt, String mockOutputText) {
        when(models.generateContent(eq(testModel), eq(expectedPrompt), any()))
                .thenReturn(generateContentResponse);
        when(generateContentResponse.text()).thenReturn(mockOutputText);
    }

    @Test
    void generateWeeklyAdvice_validData_returnsAdvice() {
        String username = "fitUser";
        String generatedPrompt = "prompt";
        String aiResponse = "response";
        when(promptBuilder.advicePrompt(username, "english")).thenReturn(generatedPrompt);
        mockGeminiResponse(generatedPrompt, aiResponse);

        String result = geminiService.generateWeeklyAdvice(username, "english");

        assertNotNull(result);
        assertEquals(aiResponse, result);
        verify(promptBuilder, times(1)).advicePrompt(username, "english");
    }

    @Test
    void answerHealthQuestion_validData_returnsAnswer() {
        String question = "question";
        String generatedPrompt = "prompt";
        String aiResponse = "response";
        when(promptBuilder.questionPrompt(question)).thenReturn(generatedPrompt);
        mockGeminiResponse(generatedPrompt, aiResponse);

        String result = geminiService.answerHealthQuestion("fitUser", question);

        assertNotNull(result);
        assertEquals(aiResponse, result);
        verify(promptBuilder, times(1)).questionPrompt(question);
    }

    @Test
    void generateHealthPrediction_validRequest_returnsPrognose() {
        String username = "fitUser";
        String generatedPrompt = "prompt";
        String aiResponse = "response";
        when(promptBuilder.predictionPrompt(username, "english")).thenReturn(generatedPrompt);
        mockGeminiResponse(generatedPrompt, aiResponse);

        String result = geminiService.generateHealthPrediction(username, "english");

        assertNotNull(result);
        assertEquals(aiResponse, result);
        verify(promptBuilder, times(1)).predictionPrompt(username, "english");
    }
}
