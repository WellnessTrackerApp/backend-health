package pl.edu.healthapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.healthapp.dto.request.ActivityCreationDTO;
import pl.edu.healthapp.dto.response.ActivityDTO;
import pl.edu.healthapp.mapper.ActivityMapper;
import pl.edu.healthapp.model.ActivityEntry;
import pl.edu.healthapp.security.CustomUserDetails;
import pl.edu.healthapp.service.ActivityService;

import java.util.List;

@RestController
@RequestMapping("/activities")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService){this.activityService = activityService;}

    @Operation(
            summary = "Add activity entry",
            description = "Add activity entry for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Activity entry created successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "409", description = "Activity entry already exists in this timeslot")
            }
    )
    @PostMapping
    public ResponseEntity<ActivityDTO> addActivity(@AuthenticationPrincipal CustomUserDetails user, @RequestBody ActivityCreationDTO activityCreationDTO){
        ActivityEntry activity = ActivityMapper.toEntity(activityCreationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ActivityMapper
                        .fromEntity(activityService
                                .addActivity(user.getUsername(), activity, true)));
    }

    @Operation(
            summary = "Delete activity entry by id",
            description = "Delete activity entry added by the currently authenticated user by activity entry id.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "activity entry deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "activity entry not found or does not belong to specified user")
            }
    )
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long activityId){
        activityService.deleteActivity(user.getUsername(), activityId, true);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Get daily activities",
            description = "Retrieves a list of all activities ending last day added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Activities list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/history/daily")
    public  List<ActivityDTO> getDailyActivities(@AuthenticationPrincipal CustomUserDetails user){
        return activityService.getDailyActivities(user.getUsername())
                            .stream()
                            .map(ActivityMapper::fromEntity)
                            .toList();
    }

    @Operation(
            summary = "Get weekly activities",
            description = "Retrieves a list of all activities ending last week added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Activities list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/history/weekly")
    public  List<ActivityDTO> getWeeklyActivities(@AuthenticationPrincipal CustomUserDetails user){
        return activityService.getWeeklyActivities(user.getUsername())
                .stream()
                .map(ActivityMapper::fromEntity)
                .toList();
    }

    @Operation(
            summary = "Get monthly activities",
            description = "Retrieves a list of all activities ending last month added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Activities list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/history/monthly")
    public  List<ActivityDTO> getMonthlyActivities(@AuthenticationPrincipal CustomUserDetails user){
        return activityService.getMonthlyActivities(user.getUsername())
                .stream()
                .map(ActivityMapper::fromEntity)
                .toList();
    }

    @Operation(
            summary = "Get yearly activities",
            description = "Retrieves a list of all activities ending last year added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Activities list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/history/yearly")
    public  List<ActivityDTO> getYearlyActivities(@AuthenticationPrincipal CustomUserDetails user){
        return activityService.getYearlyActivities(user.getUsername())
                .stream()
                .map(ActivityMapper::fromEntity)
                .toList();
    }


    @Operation(
            summary = "Get daily steps",
            description = "Returns sum of steps during all this day activities added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Daily steps retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/steps")
    public ResponseEntity<Integer> getDailySteps(@AuthenticationPrincipal CustomUserDetails user){
        int steps = activityService.getDailySteps(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(steps);
    }

    @Operation(
            summary = "Get daily calories burnt",
            description = "Returns sum of calories burnt during all this day activities added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Daily calories burnt retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/calories_burnt")
    public ResponseEntity<Integer> getDailyCaloriesBurnt(@AuthenticationPrincipal CustomUserDetails user){
        int calories = activityService.getDailyCaloriesBurnt(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(calories);
    }

    @Operation(
            summary = "Get monthly activity PDF report",
            description = "Get monthly activity report in PDF format for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Report created successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "500", description = "Failed to create a sleep report")
            }
    )
    @GetMapping("/download-report")
    public ResponseEntity<Void> downloadReport(@AuthenticationPrincipal CustomUserDetails user, HttpServletResponse response) {
        activityService.getPDFReport(user.getUsername(), response);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
