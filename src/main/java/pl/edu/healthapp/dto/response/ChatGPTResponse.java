package pl.edu.healthapp.dto.response;

import java.util.List;

public record ChatGPTResponse(List<Choice> choices) {

    public static record Choice(Message message) {

        public static record Message(String role, String content){}
    }
}