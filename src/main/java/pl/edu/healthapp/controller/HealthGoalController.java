package pl.edu.healthapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.healthapp.dto.request.HealthGoalCreationDTO;
import pl.edu.healthapp.dto.response.HealthGoalDTO;
import pl.edu.healthapp.mapper.HealthGoalMapper;
import pl.edu.healthapp.dto.response.GoalProgress;
import pl.edu.healthapp.model.HealthGoal;
import pl.edu.healthapp.model.HealthGoalType;
import pl.edu.healthapp.security.CustomUserDetails;
import pl.edu.healthapp.service.HealthGoalService;


import java.util.List;

@RestController
@RequestMapping("/goals")
public class HealthGoalController {
    private final HealthGoalService healthGoalService;

    public HealthGoalController(HealthGoalService healthGoalService){this.healthGoalService = healthGoalService;}

    @Operation(
            summary = "Set health goal",
            description = "Set health goal or update goal of the same type for currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Health goal set successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @PostMapping
    public ResponseEntity<HealthGoalDTO> setGoal(@AuthenticationPrincipal CustomUserDetails user, @RequestBody HealthGoalCreationDTO healthGoalCreationDTO){
        HealthGoal healthGoal = HealthGoalMapper.toEntity(healthGoalCreationDTO);
        System.out.println("goal mapped");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(HealthGoalMapper
                        .fromEntity(healthGoalService
                                .setGoal(user.getUsername(), healthGoal, true)));
    }

    @Operation(
            summary = "Delete health goal by type",
            description = "Delete health goal for currently authenticated user by health goal type.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Sleep entry deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "Sleep entry not found or does not belong to specified user")
            }
    )
    @DeleteMapping("/{healthGoalType}")
    public ResponseEntity<Void> deleteGoal(@AuthenticationPrincipal CustomUserDetails user, @PathVariable HealthGoalType healthGoalType){
        healthGoalService.deleteGoal(user.getUsername(), healthGoalType);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Get all health goals",
            description = "Retrieves a list of all health goals of currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Health goals list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping
    public  List<HealthGoalDTO> getGoals(@AuthenticationPrincipal CustomUserDetails user){
        return healthGoalService.getGoals(user.getUsername())
                            .stream()
                            .map(HealthGoalMapper::fromEntity)
                            .toList();
    }

    @Operation(
            summary = "Get all health goals progress",
            description = "Retrieves a list of progresses for all health goals for currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Health goals progresses list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/progress")
    public  List<GoalProgress> getProgresses(@AuthenticationPrincipal CustomUserDetails user){
        return healthGoalService.getProgressForAllGoals(user.getUsername());
    }

    @Operation(
            summary = "Get health goal progress",
            description = "Retrieves progress for health goaloff specified type for currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Health goals progresses list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/progress/{healthGoalType}")
    public  ResponseEntity<GoalProgress> getProgress(@AuthenticationPrincipal CustomUserDetails user, @PathVariable HealthGoalType healthGoalType){
        GoalProgress goalProgress =  healthGoalService.getProgress(user.getUsername(), healthGoalType);
        return ResponseEntity.status(HttpStatus.OK).body(goalProgress);
    }
}
