package com.booking.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for social login")
public class SocialLoginDto {
    @NotNull
    @Schema(description = "ID token obtained from the social authentication provider", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjU4Ym...")
    private String idToken;
}
