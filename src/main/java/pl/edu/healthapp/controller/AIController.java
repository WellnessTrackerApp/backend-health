package pl.edu.healthapp.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.healthapp.security.CustomUserDetails;
import pl.edu.healthapp.service.AIService;



@RestController
@RequestMapping("/ai")
public class AIController {
    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @Operation(
            summary = "Get advice from AI assistant",
            description = "Get advice from AI assistant based on the currently authenticated user last month data.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Advice retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/advice")
    public ResponseEntity<String> getWeeklyAdvice(@AuthenticationPrincipal CustomUserDetails user) {
        String advice = aiService.generateWeeklyAdvice(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(advice);
    }

    @Operation(
            summary = "Ask AI assistant a question",
            description = "Ask AI assistant any health question.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Answer retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/ask")
    public ResponseEntity<String> askHealthQuestion(@AuthenticationPrincipal CustomUserDetails user, @RequestBody String question) {
        String answer = aiService.answerHealthQuestion(user.getUsername(), question);
        return ResponseEntity.status(HttpStatus.OK).body(answer);
    }

    @Operation(
            summary = "Get health prediction from AI assistant",
            description = "Get health prediction from AI assistant based on the currently authenticated user last month data.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Prediction retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/prediction")
    public ResponseEntity<String> getHealthPrediction(@AuthenticationPrincipal CustomUserDetails user){
        String prediction = aiService.generateHealthPrediction(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(prediction);
    }
}
