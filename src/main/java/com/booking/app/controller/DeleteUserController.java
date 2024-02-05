package com.booking.app.controller;

import com.booking.app.controller.api.DeleteUserAPI;
import com.booking.app.repositories.UserCredentialsRepository;
import com.booking.app.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.booking.app.constant.CustomHttpHeaders.REMEMBER_ME;
import static com.booking.app.constant.CustomHttpHeaders.USER_ID;
import static com.booking.app.constant.JwtTokenConstants.REFRESH_TOKEN;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Log4j2
public class DeleteUserController implements DeleteUserAPI {

    private final UserCredentialsRepository userCredentialsRepository;

    @DeleteMapping("/delete-user")
    public ResponseEntity<?> deleteUser(HttpServletRequest request, HttpServletResponse response) {
        String userId = CookieUtils.getCookie(request, USER_ID).orElseThrow().getValue();
        userCredentialsRepository.deleteById(UUID.fromString(userId));
        CookieUtils.deleteCookie(request, response, REFRESH_TOKEN);
        CookieUtils.deleteCookie(request, response, USER_ID);
        CookieUtils.deleteCookie(request, response, REMEMBER_ME);
        return ResponseEntity.ok().build();
    }

}
