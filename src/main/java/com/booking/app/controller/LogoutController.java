package com.booking.app.controller;

import com.booking.app.constant.CorsConfigConstants;
import com.booking.app.controller.api.LogoutAPI;
import com.booking.app.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling user logout functionality.
 * Implements the LogoutAPI interface.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@CrossOrigin(origins = {CorsConfigConstants.ALLOWED_ORIGIN_80,CorsConfigConstants.ALLOWED_ORIGIN_81}, maxAge = 3600,
        exposedHeaders = {CorsConfigConstants.EXPOSED_HEADER_REFRESH_TOKEN, CorsConfigConstants.EXPOSED_HEADER_AUTHORIZATION})
public class LogoutController implements LogoutAPI {

    /**
     * Handles HTTP POST request to '/logout'.
     * Clears the security context and invalidates the user's authentication.
     *
     * @return ResponseEntity with an HTTP status representing successful logout
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request,response,"refresh-token");
        SecurityContext context = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
        context.setAuthentication(null);

        return ResponseEntity.ok().build();
    }

}
