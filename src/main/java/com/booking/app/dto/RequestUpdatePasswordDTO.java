package com.booking.app.dto;

import com.booking.app.annotation.PasswordMatches;
import com.booking.app.util.ConfirmPasswordUtil;
import io.swagger.v3.oas.annotations.media.Schema;
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
@PasswordMatches
@Schema(description = "Data Transfer Object for updating user passwords, including the last password, new password, and password confirmation.")
public class RequestUpdatePasswordDTO implements ConfirmPasswordUtil {

    @NotNull
    @Schema(description = "The user's last password.", required = true)
    private String lastPassword;

    @NotBlank(message = "Empty password")
    @NotNull(message = "Password is NULL")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,30}$", message = "Password must be of 8 - 30 characters and contain at least one letter and one number")
    @Schema(description = "The new password for the user.", example = "StrongPassword!123", required = true)
    private String password;

    @NotBlank(message = "Empty password")
    @NotNull(message = "Password is NULL")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,30}$", message = "Password must be of 8 - 30 characters and contain at least one letter and one number")
    @Schema(description = "The confirmation of the new password.", example = "StrongPassword!123", required = true)
    private String confirmPassword;
}
