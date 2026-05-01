package pl.edu.healthapp.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.healthapp.dto.request.UserCreationDTO;
import pl.edu.healthapp.dto.response.UserDTO;
import pl.edu.healthapp.mapper.UserMapper;
import pl.edu.healthapp.model.User;
import pl.edu.healthapp.service.UserService;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class PublicController {
    private final UserService userService;

    public PublicController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Register user",
            description = "Register new user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "409", description = "User with such username or email already exists")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserCreationDTO userCreationDTO){
        User user = UserMapper.toEntity(userCreationDTO);
        UserDTO userDTO = UserMapper.fromEntity(userService.registerUser(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }
}
