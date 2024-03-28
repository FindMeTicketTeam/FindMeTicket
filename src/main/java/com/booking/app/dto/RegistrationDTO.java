package com.booking.app.dto;

import com.booking.app.annotation.PasswordMatches;
import com.booking.app.util.ConfirmPasswordUtil;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@PasswordMatches
@NoArgsConstructor
public class RegistrationDTO implements ConfirmPasswordUtil {

    @NotBlank(message = "Invalid Login: Empty login")
    @NotNull(message = "Invalid Login: Login is NULL")
    @Size(min = 5, max = 30, message = "Invalid Login: Must be of 5 - 30 characters")
    private String username;

    @NotNull(message = "Email is NULL")
    @Email(message = "This is not a valid email format")
    private String email;

    @NotBlank(message = "Empty password")
    @NotNull(message = "Password is NULL")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,30}$", message = "Password must be of 8 - 30 characters and contain at least one letter and one number")
    private String password;

    private String confirmPassword;

    private Boolean notification;

}


