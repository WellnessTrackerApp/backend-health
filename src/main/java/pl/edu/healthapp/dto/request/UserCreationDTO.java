package pl.edu.healthapp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import pl.edu.healthapp.model.Gender;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserCreationDTO(
        @NotBlank(message = "Userid is required")
        UUID id,
        @NotBlank(message = "Username is required")
        String username,
        @Email
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        String password,
        @NotNull(message = "Birthday date is required")
        @PastOrPresent(message = "Birthday cannot be in the future")
        OffsetDateTime birthDate,
        @NotBlank(message = "Height is required")
        double height,
        @NotBlank(message = "Weight is required")
        double weight,
        @NotBlank(message = "Gender is required")
        Gender gender) { }
