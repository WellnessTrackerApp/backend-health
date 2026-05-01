package pl.edu.healthapp.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.healthapp.dto.request.UserUpdateDTO;
import pl.edu.healthapp.dto.response.UserDTO;
import pl.edu.healthapp.mapper.UserMapper;
import pl.edu.healthapp.security.CustomUserDetails;
import pl.edu.healthapp.service.UserService;


@RestController
@RequestMapping("/me")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Update user",
            description = "Update weight or height for currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User updated successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @PutMapping
    public ResponseEntity<UserDTO> updateUser(@AuthenticationPrincipal CustomUserDetails user, @RequestBody UserUpdateDTO userUpdateDTO) {
        UserDTO userDTO = UserMapper.fromEntity(userService.updateUser(user.getUsername(), userUpdateDTO, true));
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

    @Operation(
            summary = "Delete user",
            description = "Delete currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal CustomUserDetails user) {
        userService.deleteUser(user.getUsername());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Get user",
            description = "Retrieved currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping
    public ResponseEntity<UserDTO> getUser(@AuthenticationPrincipal CustomUserDetails user) {
        UserDTO userDTO = UserMapper.fromEntity(userService.findByUsername(user.getUsername()));
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }
}
