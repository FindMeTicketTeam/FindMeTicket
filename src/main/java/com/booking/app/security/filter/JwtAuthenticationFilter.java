package com.booking.app.security.filter;

import com.booking.app.entity.User;
import com.booking.app.exception.exception.UserNotFoundException;
import com.booking.app.util.CookieUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

import static com.booking.app.constant.CustomHttpHeaders.USER_ID;
import static com.booking.app.constant.JwtTokenConstants.BEARER;
import static com.booking.app.constant.JwtTokenConstants.REFRESH_TOKEN;

/**
 * This filter intercepts incoming requests and processes JWT authentication.
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    private final UserDetailsService userDetailsService;

    private final RedirectStrategy redirectStrategy;

    private UserDetails userDetails;

    private static final String LOGOUT_ENDPOINT = "/logout";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // If the request is for the logout endpoint, proceed with the filter chain
        if (request.getRequestURI().equals(LOGOUT_ENDPOINT)) {
            chain.doFilter(request, response);
            return;
        }

        // Extract access token and refresh token from the request
        String accessToken = JwtProvider.extractAccessToken(request);
        String refreshToken = JwtProvider.extractRefreshToken(request);

        // Check if any token has been provided for authenticated requests
        if (accessToken == null || refreshToken == null) {
            log.warn("JWT token/tokens hasn't been provided");
        }
        // Check if refresh token is invalid and email is extracted from toekn
        else if (Objects.isNull(jwtProvider.extractEmail(refreshToken))) {
            log.warn("Invalid JWT refresh token has been provided");
            // Redirect to the logout endpoint
            redirectStrategy.sendRedirect(request, response, LOGOUT_ENDPOINT);
            return;
        }
        // Check if user is present by provided email
        else if (Objects.isNull(getUserDetails(jwtProvider.extractEmail(refreshToken)))) {
            log.warn("No user is present");
        } else {
            User user = (User) userDetails;
            String email = user.getEmail();

            // Validate access token
            if (jwtProvider.validateAccessToken(accessToken)) {
                handleValidAccessToken(response, refreshToken, userDetails, email);
            } else {
                try {
                    // Validate refresh token
                    jwtProvider.validateRefreshToken(refreshToken);
                    handleValidRefreshToken(response, userDetails, email);
                } catch (ExpiredJwtException e) {
                    // If refresh token is expired, redirect to the logout endpoint
                    redirectStrategy.sendRedirect(request, response, LOGOUT_ENDPOINT);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * Refresh token is valid, so it takes just to generate new access token and update alive time for refresh token
     *
     * @param response     The HttpServletResponse object to set cookies and headers.
     * @param refreshToken The living refresh token.
     * @param userDetails  The UserDetails object associated with the user.
     * @param email        The user's email.
     */
    private void handleValidAccessToken(HttpServletResponse response, String refreshToken, UserDetails userDetails, String email) {
        String newAccessToken = jwtProvider.generateAccessToken(email);
        setAuthenticationResponseData(response, newAccessToken, refreshToken, userDetails);
    }

    /**
     * Generate new access and refresh tokens when the access token is no longer valid.
     *
     * @param response    The HttpServletResponse object to set cookies and headers.
     * @param userDetails The UserDetails object associated with the user.
     * @param email       The user's email.
     */
    private void handleValidRefreshToken(HttpServletResponse response, UserDetails userDetails, String email) {
        String newAccessToken = jwtProvider.generateAccessToken(email);
        String newRefreshToken = jwtProvider.generateRefreshToken(email);
        setAuthenticationResponseData(response, newAccessToken, newRefreshToken, userDetails);
    }

    /**
     * Extract UserDetails and if such is present, then set it on SecurityContextHolder.
     *
     * @param email The email obtained from the token
     * @return UserDetails from the database, or null if not found
     */
    private UserDetails getUserDetails(String email) {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(email);
            setSecurityContext(user);
            this.userDetails = user;
            return user;
        } catch (UserNotFoundException e) {
            return null;
        }
    }

    private void setAuthenticationResponseData(HttpServletResponse response, String accessToken, String refreshToken, UserDetails userDetails) {
        CookieUtils.addCookie(response, REFRESH_TOKEN, refreshToken, jwtProvider.getRefreshTokenExpirationMs(), true, true);
        CookieUtils.addCookie(response, USER_ID, ((User) userDetails).getId().toString(), jwtProvider.getRefreshTokenExpirationMs(), false, true);
        response.setHeader(HttpHeaders.AUTHORIZATION, BEARER + accessToken);
    }

    private void setSecurityContext(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }

}
