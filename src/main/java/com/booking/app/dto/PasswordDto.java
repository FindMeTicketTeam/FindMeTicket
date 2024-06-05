package com.booking.app.dto;

import com.booking.app.annotation.PasswordMatches;
import com.booking.app.util.ConfirmPasswordUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@PasswordMatches
@Schema(description = "Data Transfer Object for password operations, including email, verification code, new password, and password confirmation.")
public class PasswordDto implements ConfirmPasswordUtil {

    @Email
    @NotNull
    @Schema(description = "The user's email address.", example = "user@example.com", required = true)
    private String email;

    @NotNull
    @Schema(description = "The verification code sent to the user's email.", example = "123456", required = true)
    private String code;

    @NotNull
    @Schema(description = "The new password for the user.", example = "StrongPassword!123", required = true)
    private String password;

    @NotNull
    @Schema(description = "The confirmation of the new password.", example = "StrongPassword!123", required = true)
    private String confirmPassword;

}