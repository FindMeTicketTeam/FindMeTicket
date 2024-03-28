package com.booking.app.controller;

import com.booking.app.controller.api.LoginAPI;
import com.booking.app.dto.AuthorizedUserDTO;
import com.booking.app.dto.LoginDTO;
import com.booking.app.dto.OAuth2IdTokenDTO;
import com.booking.app.entity.UserCredentials;
import com.booking.app.security.jwt.JwtProvider;
import com.booking.app.services.GoogleAccountService;
import com.booking.app.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import static com.booking.app.constant.CustomHttpHeaders.REMEMBER_ME;
import static com.booking.app.constant.CustomHttpHeaders.USER_ID;
import static com.booking.app.constant.JwtTokenConstants.BEARER;
import static com.booking.app.constant.JwtTokenConstants.REFRESH_TOKEN;

/**
 * LoginController handles authentication-related operations.
 * This controller provides endpoints for user authentication.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Log4j2
public class LoginController implements LoginAPI {

    private final AuthenticationManager authenticationManager;

    private final JwtProvider jwtProvider;

    private final GoogleAccountService googleAccountServiceImpl;

    /**
     * Handles user sign-in request.
     *
     * @param loginDTO The LoginDTO containing user credentials.
     * @param request  The HttpServletRequest containing the request information.
     * @param response The HttpServletResponse containing the response information.
     * @return ResponseEntity indicating the success of the sign-in operation.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        if (response.getHeader(HttpHeaders.SET_COOKIE) != null && response.getHeader(HttpHeaders.AUTHORIZATION) != null)
            return ResponseEntity.ok().build();

        if (authentication.isAuthenticated()) {
            if (Boolean.TRUE.equals(loginDTO.getRememberMe())) {
                CookieUtils.addCookie(response, REMEMBER_ME, loginDTO.getRememberMe().toString(), jwtProvider.getRefreshTokenExpirationMs(), true, true);
            }

            UserCredentials userCredentials = (UserCredentials) authentication.getPrincipal();

            String refreshToken = jwtProvider.generateRefreshToken(loginDTO.getEmail());
            String accessToken = jwtProvider.generateAccessToken(loginDTO.getEmail());

            CookieUtils.addCookie(response, REFRESH_TOKEN, refreshToken, jwtProvider.getRefreshTokenExpirationMs(), true, true);
            CookieUtils.addCookie(response, USER_ID, userCredentials.getId().toString(), 10000000, false, true);
            response.setHeader(HttpHeaders.AUTHORIZATION, BEARER + accessToken);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);

            AuthorizedUserDTO authorizedUserDTO = AuthorizedUserDTO.builder()
                    .email(userCredentials.getEmail())
                    .notification(userCredentials.getUser().getNotification())
                    .basicPicture(Base64.getEncoder().encodeToString(userCredentials.getUser().getProfilePicture()))
                    .username(userCredentials.getUsername()).build();

            return ResponseEntity.ok().body(authorizedUserDTO);
        }
        return ResponseEntity.status(401).build();
    }

    /**
     * Handles OAuth2 authentication with ID token.
     *
     * @param tokenDTO The OAuth2IdTokenDTO containing the ID token.
     * @param response The HttpServletResponse used to set cookies and headers in the HTTP response.
     * @return ResponseEntity with HTTP status 200 if authentication is successful, or
     * ResponseEntity with HTTP status 401 if authentication fails.
     * @throws GeneralSecurityException If a security error occurs during ID token verification.
     * @throws IOException              If an I/O error occurs during ID token verification.
     */
    @PostMapping("/oauth2/authorize/*")
    public ResponseEntity<?> loginOAuth2(@RequestBody OAuth2IdTokenDTO tokenDTO, HttpServletResponse response) {
        AtomicReference<AuthorizedUserDTO> authorizedUserDTO = new AtomicReference<>();
        googleAccountServiceImpl.loginOAuthGoogle(tokenDTO).

                ifPresentOrElse(
                        userCredentials -> {
                            String refreshToken = jwtProvider.generateRefreshToken(userCredentials.getEmail());
                            String accessToken = jwtProvider.generateAccessToken(userCredentials.getEmail());

                            CookieUtils.addCookie(response, REFRESH_TOKEN, refreshToken, jwtProvider.getRefreshTokenExpirationMs(), true, true);
                            CookieUtils.addCookie(response, USER_ID, userCredentials.getId().toString(), jwtProvider.getRefreshTokenExpirationMs(), false, true);
                            response.setHeader(HttpHeaders.AUTHORIZATION, BEARER + accessToken);

                            SecurityContext context = SecurityContextHolder.createEmptyContext();
                            context.setAuthentication(new UsernamePasswordAuthenticationToken(userCredentials.getEmail(), userCredentials.getUsername(), userCredentials.getAuthorities()));
                            SecurityContextHolder.setContext(context);
                            authorizedUserDTO.set(AuthorizedUserDTO.builder()
                                    .email(userCredentials.getEmail())
                                    .notification(userCredentials.getUser().getNotification())
                                    .username(userCredentials.getUsername())
                                    .googlePicture(userCredentials.getUser().getUrlPicture()).build());

                        },
                        () -> {
                            try {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Client ID is not right");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );

        return ResponseEntity.ok().body(authorizedUserDTO);
    }
}
