package pl.edu.healthapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.healthapp.dto.request.DietCreationDTO;
import pl.edu.healthapp.dto.response.DietDTO;
import pl.edu.healthapp.mapper.DietMapper;
import pl.edu.healthapp.model.DietEntry;
import pl.edu.healthapp.model.Macronutrients;
import pl.edu.healthapp.security.CustomUserDetails;
import pl.edu.healthapp.service.DietService;

import java.util.List;

@RestController
@RequestMapping("/diet")
public class DietController {
    private final DietService dietService;

    public DietController(DietService dietService){this.dietService = dietService;}

    @Operation(
            summary = "Add meal",
            description = "Add diet entry for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Diet entry created successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @PostMapping
    public ResponseEntity<DietDTO> addMeal(@AuthenticationPrincipal CustomUserDetails user, @RequestBody DietCreationDTO dietCreationDTO){
        DietEntry diet = DietMapper.toEntity(dietCreationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DietMapper
                        .fromEntity(dietService
                                .addMeal(user.getUsername(), diet, true)));
    }

    @Operation(
            summary = "Delete diet entry by id",
            description = "Delete diet entry added by the currently authenticated user by diet entry id.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Diet entry deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "Diet entry not found or does not belong to specified user")
            }
    )
    @DeleteMapping("/{mealId}")
    public ResponseEntity<Void> deleteMeal(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long mealId){
        dietService.deleteMeal(user.getUsername(), mealId, true);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Get daily diet entries",
            description = "Retrieves a list of all diet entries ending the last 24 hour added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Diet entries list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping
    public  List<DietDTO> getDailyDiet(@AuthenticationPrincipal CustomUserDetails user){
        return dietService.getDailyDiet(user.getUsername())
                            .stream()
                            .map(DietMapper::fromEntity)
                            .toList();
    }

    @Operation(
            summary = "Get daily calories consumed",
            description = "Returns sum of calories of all this day diet entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Daily calories consumed retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/calories")
    public ResponseEntity<Integer> getDailyCalories(@AuthenticationPrincipal CustomUserDetails user){
        int calories = dietService.getDailyCalories(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(calories);
    }

    @Operation(
            summary = "Get daily proteins consumed",
            description = "Returns sum of proteins in gr of all this day diet entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Daily proteins consumed retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/proteins")
    public ResponseEntity<Double> getDailyProteins(@AuthenticationPrincipal CustomUserDetails user){
        double proteins = dietService.getDailyProteins(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(proteins);
    }

    @Operation(
            summary = "Get daily carbs consumed",
            description = "Returns sum of carbs in gr of all this day diet entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Daily carbs consumed retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/carbs")
    public ResponseEntity<Double> getDailyCarbs(@AuthenticationPrincipal CustomUserDetails user){
        double carbs = dietService.getDailyCarbs(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(carbs);
    }

    @Operation(
            summary = "Get daily fat consumed",
            description = "Returns sum of fat in gr of all this day diet entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Daily proteins consumed retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/fats")
    public ResponseEntity<Double> getDailyFats(@AuthenticationPrincipal CustomUserDetails user){
        double fats = dietService.getDailyFats(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(fats);
    }

    @Operation(
            summary = "Get daily macro consumed",
            description = "Returns sum of macros of all this day diet entries added by the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Daily macro consumed retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping("/macro")
    public ResponseEntity<Macronutrients> getDailyMacro(@AuthenticationPrincipal CustomUserDetails user){
        Macronutrients macros = dietService.getDailyMacros(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(macros);
    }

    @Operation(
            summary = "Get monthly diet PDF report",
            description = "Get monthly diet report in PDF format for the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Report created successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "500", description = "Failed to create a sleep report")
            }
    )
    @GetMapping("/download-report")
    public ResponseEntity<Void> downloadReport(@AuthenticationPrincipal CustomUserDetails user, HttpServletResponse response) {
        dietService.getPDFReport(user.getUsername(), response);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
