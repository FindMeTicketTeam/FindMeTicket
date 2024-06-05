package com.booking.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for user login")
public class LoginDto {

    @NotNull(message = "Email is NULL")
    @Email(message = "This is not a valid email format")
    @Schema(description = "Email address of the user", example = "user@example.com")
    private String email;

    @NotBlank(message = "Empty password")
    @NotNull(message = "Password is NULL")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,30}$", message = "Password must be of 8 - 30 characters and contain at least one letter and one number")
    @Schema(description = "Password of the user, must be between 8 and 30 characters long and contain at least one letter and one number", example = "Password123")
    private String password;

    @NotNull
    @Builder.Default
    @Schema(description = "Remember me option for the user", example = "false", defaultValue = "false")
    private Boolean rememberMe = false;

}
