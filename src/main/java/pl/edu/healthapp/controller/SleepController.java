package pl.edu.healthapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.healthapp.dto.request.SleepCreationDTO;
import pl.edu.healthapp.dto.response.SleepDTO;
import pl.edu.healthapp.mapper.SleepMapper;
import pl.edu.healthapp.model.SleepEntry;
import pl.edu.healthapp.model.SleepQuality;
import pl.edu.healthapp.security.CustomUserDetails;
import pl.edu.healthapp.service.SleepService;

import java.util.List;

@RestController
@RequestMapping("/sleep")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class SleepController {
    private final SleepService sleepService;

    public SleepController(SleepService sleepService){this.sleepService = sleepService;}


    @Operation(
            summary = "Add sleep entry",
            description = "Add sleep entry for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Sleep entry created successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "409", description = "Sleep entry already exists in this timeslot")
            }
    )
    @PostMapping
    public ResponseEntity<SleepDTO> addSleep(@AuthenticationPrincipal CustomUserDetails user, @RequestBody SleepCreationDTO sleepCreationDTO){
        SleepEntry sleep = SleepMapper.toEntity(sleepCreationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SleepMapper
                        .fromEntity(sleepService
                                .addSleep(user.getUsername(), sleep, true)));
    }

    @Operation(
            summary = "Delete sleep entry by id",
            description = "Delete sleep entry added by the currently authenticated user by sleep entry id.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Sleep entry deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "Sleep entry not found or does not belong to specified user")
            }
    )
    @DeleteMapping("/{sleepId}")
    public ResponseEntity<Void> deleteSleep(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long sleepId){
        sleepService.deleteSleep(user.getUsername(), sleepId, true);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Get daily sleep entries",
            description = "Retrieves a list of all sleep entries ending the last 24 hour added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sleep entries list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/daily")
    public List<SleepDTO> getDailySleepHistory(@AuthenticationPrincipal CustomUserDetails user){
        return sleepService.getDailySleepHistory(user.getUsername())
                            .stream()
                            .map(SleepMapper::fromEntity)
                            .toList();
    }

    @Operation(
            summary = "Get weekly sleep entries",
            description = "Retrieves a list of all sleep entries ending last week added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sleep records list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/weekly")
    public List<SleepDTO> getWeeklySleepHistory(@AuthenticationPrincipal CustomUserDetails user){
        return sleepService.getWeeklySleepHistory(user.getUsername())
                            .stream()
                            .map(SleepMapper::fromEntity)
                            .toList();
    }

    @Operation(
            summary = "Get monthly sleep entries",
            description = "Retrieves a list of all sleep entries ending last month added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sleep records list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/monthly")
    public List<SleepDTO> getMonthlySleepHistory(@AuthenticationPrincipal CustomUserDetails user){
        return sleepService.getMonthlySleepHistory(user.getUsername())
                            .stream()
                            .map(SleepMapper::fromEntity)
                            .toList();
    }

    @Operation(
            summary = "Get yearly sleep entries",
            description = "Retrieves a list of all sleep entries ending last year added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sleep records list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/yearly")
    public List<SleepDTO> getYearlySleepHistory(@AuthenticationPrincipal CustomUserDetails user){
        return sleepService.getYearlySleepHistory(user.getUsername())
                            .stream()
                            .map(SleepMapper::fromEntity)
                            .toList();
    }

    @Operation(
            summary = "Get daily sleep duration",
            description = "Returns sum of durations in hours of all this day sleep entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Daily sleep duration retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/duration")
    public ResponseEntity<Long> getSleepDuration(@AuthenticationPrincipal CustomUserDetails user){
        Long duration = sleepService.getDailySleepDurationInHours(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(duration);
    }


    @Operation(
            summary = "Get median daily sleep duration",
            description = "Returns median duration in hours of all this day sleep entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Median sleep duration retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "No sleep found for current user")
            }
    )
    @GetMapping("/daily/median_duration")
    public ResponseEntity<Double> getDailySleepMedianDuration(@AuthenticationPrincipal CustomUserDetails user){
        double duration = sleepService.getDailyMedianSleepDuration(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(duration);
    }

    @Operation(
            summary = "Get median weekly sleep duration",
            description = "Returns median duration in hours of all this week sleep entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Median sleep duration retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "No sleep found for current user")
            }
    )
    @GetMapping("/weekly/median_duration")
    public ResponseEntity<Double> getWeeklySleepMedianDuration(@AuthenticationPrincipal CustomUserDetails user){
        double duration = sleepService.getWeeklyMedianSleepDuration(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(duration);
    }

    @Operation(
            summary = "Get median monthly sleep duration",
            description = "Returns median duration in hours of all this month sleep entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Median sleep duration retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "No sleep found for current user")
            }
    )
    @GetMapping("/monthly/median_duration")
    public ResponseEntity<Double> getMonthlySleepMedianDuration(@AuthenticationPrincipal CustomUserDetails user){
        double duration = sleepService.getMonthlyMedianSleepDuration(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(duration);
    }

    @Operation(
            summary = "Get median yearly sleep duration",
            description = "Returns median duration in hours of all this year sleep entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Median sleep duration retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "No sleep found for current user")
            }
    )
    @GetMapping("/yearly/median_duration")
    public ResponseEntity<Double> getYearlySleepMedianDuration(@AuthenticationPrincipal CustomUserDetails user){
        double duration = sleepService.getYearlyMedianSleepDuration(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(duration);
    }


    @Operation(
            summary = "Get median daily sleep quality",
            description = "Returns median quality of all this day sleep entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Median sleep quality retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "No sleep found for current user")
            }
    )
    @GetMapping("/daily/median_quality")
    public ResponseEntity<String> getDailySleepMedianQuality(@AuthenticationPrincipal CustomUserDetails user){
        SleepQuality quality = sleepService.getDailyMedianSleepQuality(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(String.valueOf(quality));
    }

    @Operation(
            summary = "Get median weekly sleep quality",
            description = "Returns median quality of all this week sleep entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Median sleep quality retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "No sleep found for current user")
            }
    )
    @GetMapping("/weekly/median_quality")
    public ResponseEntity<String> getWeeklySleepMedianQuality(@AuthenticationPrincipal CustomUserDetails user){
        SleepQuality quality = sleepService.getWeeklyMedianSleepQuality(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(String.valueOf(quality));
    }

    @Operation(
            summary = "Get median monthly sleep quality",
            description = "Returns median quality of all this month sleep entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Median sleep quality retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "No sleep found for current user")
            }
    )
    @GetMapping("/monthly/median_quality")
    public ResponseEntity<String> getMonthlySleepMedianQuality(@AuthenticationPrincipal CustomUserDetails user){
        SleepQuality quality = sleepService.getMonthlyMedianSleepQuality(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(String.valueOf(quality));
    }

    @Operation(
            summary = "Get median yearly sleep quality",
            description = "Returns median quality of all this year sleep entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Median sleep quality retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "No sleep found for current user")
            }
    )
    @GetMapping("/yearly/median_quality")
    public ResponseEntity<String> getYearlySleepMedianQuality(@AuthenticationPrincipal CustomUserDetails user){
        SleepQuality quality = sleepService.getYearlyMedianSleepQuality(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(String.valueOf(quality));
    }


    @Operation(
            summary = "Get monthly sleep PDF report",
            description = "Get monthly sleep report in PDF format for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Report created successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "500", description = "Failed to create a sleep report")
            }
    )
    @GetMapping("/download-report")
    public ResponseEntity<Void> downloadReport(@AuthenticationPrincipal CustomUserDetails user, HttpServletResponse response) {
        sleepService.getPDFReport(user.getUsername(), response);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
