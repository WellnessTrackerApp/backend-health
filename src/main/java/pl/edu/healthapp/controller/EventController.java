package pl.edu.healthapp.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.healthapp.security.CustomUserDetails;
import pl.edu.healthapp.service.EventService;

import java.util.List;


@RestController
@CrossOrigin(origins = "${cors.allowed-origins}")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(
            summary = "Undo last change",
            description = "Undo last change made by currently authenticated user last month data.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Action undone successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @PostMapping("/undo")
    public ResponseEntity<Void> undoLastEvent(@AuthenticationPrincipal CustomUserDetails user) {
        eventService.undoLast(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Get weight change history",
            description = "Get weight change history of currently authenticated user last month data.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Weight list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/weight_history")
    public List<Double> getWeightChangeHistory(@AuthenticationPrincipal CustomUserDetails user) {
        return eventService.getWeightHistory(user.getUsername());
    }

    @Operation(
            summary = "Get height change history",
            description = "Get height change history of currently authenticated user last month data.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Height list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/height_history")
    public List<Double> getHeightChangeHistory(@AuthenticationPrincipal CustomUserDetails user) {
        return eventService.getHeightHistory(user.getUsername());
    }
}
