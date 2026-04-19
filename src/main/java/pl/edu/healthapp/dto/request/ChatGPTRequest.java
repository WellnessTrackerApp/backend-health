package pl.edu.healthapp.dto.request;

import java.util.List;

public record ChatGPTRequest(
        String model,
        List<ChatMessage> messages) {
    public static record ChatMessage (String role, String content){}
}
