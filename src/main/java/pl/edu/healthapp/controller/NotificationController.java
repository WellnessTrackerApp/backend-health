package pl.edu.healthapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.healthapp.dto.response.NotificationDTO;
import pl.edu.healthapp.mapper.NotificationMapper;
import pl.edu.healthapp.security.CustomUserDetails;
import pl.edu.healthapp.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService){this.notificationService = notificationService;}

    @Operation(
            summary = "Get all notifications",
            description = "Retrieves a list of notifications for currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notification list retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping
    public List<NotificationDTO> getNotifications(@AuthenticationPrincipal CustomUserDetails user) {
        return notificationService.getUserNotifications(user.getUsername())
                                    .stream()
                                    .map(NotificationMapper::fromEntity)
                                    .toList();
    }

    /*@GetMapping("/all")
    public List<NotificationDTO> getAllNotifications(@AuthenticationPrincipal CustomUserDetails user) {
        return notificationService.findAll()
                .stream()
                .map(NotificationMapper::fromEntity)
                .toList();
    }*/

    @Operation(
            summary = "Delete notification by id",
            description = "Delete notification by id for currently authenticated user by sleep entry id.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "404", description = "Notification not found or does not belong to specified user")
            }
    )
    @DeleteMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) {
        notificationService.markAsRead(user.getUsername(), id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
