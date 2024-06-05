package com.booking.app.dto;

import com.booking.app.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for Authorized User")
public class AuthenticatedUserDto {

    @Schema(description = "Username of the authorized user", example = "john_doe")
    private String username;

    @Schema(description = "Email address of the authorized user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Base64 encoded default avatar image of the authorized user", example = "iVBORw0KGgoAAAANSUhEUgAAAAUA")
    private String defaultAvatar;

    @Schema(description = "URL of the social media avatar image of the authorized user", example = "https://example.com/avatar.jpg")
    private String socialMediaAvatar;

    @Schema(description = "Notification preference of the authorized user", example = "true")
    private Boolean notification;

    public static AuthenticatedUserDto createBasicAuthorizedUser(User user) {
        return AuthenticatedUserDto.builder()
                .email(user.getEmail())
                .notification(user.isNotification())
                .defaultAvatar(Base64.getEncoder().encodeToString(user.getDefaultAvatar()))
                .username(user.getUsername()).build();
    }

    public static AuthenticatedUserDto createGoogleAuthorizedUser(User user) {
        return AuthenticatedUserDto.builder()
                .email(user.getEmail())
                .notification(user.isNotification())
                .username(user.getUsername())
                .socialMediaAvatar(user.getSocialMediaAvatar()).build();
    }

}
