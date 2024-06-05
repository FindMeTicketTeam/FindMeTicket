package com.booking.app.services;

import com.booking.app.dto.LoginDto;
import com.booking.app.dto.SocialLoginDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface LoginService {
    /**
     * Handles basic login authentication.
     * <p>
     * This method authenticates the user using the provided login credentials. If the user is already
     * logged in, it returns an HTTP 200 OK response. If the login credentials are correct, it authenticates
     * the user, sets the authentication context, and returns an HTTP 200 OK response with the user details.
     * If the credentials are incorrect, it returns an HTTP 401 Unauthorized response.
     *
     * @param loginDTO the login data transfer object containing the user's email and password
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a ResponseEntity containing the authentication result:
     * - HTTP 200 OK with the user details if authentication is successful
     * - HTTP 401 Unauthorized if authentication fails
     */
    ResponseEntity<?> loginWithEmailAndPassword(LoginDto loginDTO, HttpServletRequest request, HttpServletResponse response);


    /**
     * Handles social authentication using Google OAuth2.
     *
     * @param tokenDTO the OAuth2 token data transfer object containing the token from Google
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a ResponseEntity containing the authentication result:
     * - HTTP 200 OK with the user details if authentication is successful
     * - HTTP 401 Unauthorized if authentication fails
     */
    ResponseEntity<?> loginWithGoogle(SocialLoginDto tokenDTO, HttpServletRequest request, HttpServletResponse response);
}
