package com.booking.app.controller;

import com.booking.app.controller.api.NotificationApi;
import com.booking.app.services.NotificationService;
import com.booking.app.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.booking.app.constant.CustomHttpHeaders.USER_ID;

@RestController
@RequiredArgsConstructor
@Log4j2
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @GetMapping("/notifications/disable")
    @PreAuthorize("#{hasAnyRole('USER', 'ADMIN')}")
    @Override
    public ResponseEntity<?> disableNotification(HttpServletRequest request) {
        if (CookieUtils.getCookie(request, USER_ID).isPresent()) {
            notificationService.disable(CookieUtils.getCookie(request, USER_ID).get());
            return ResponseEntity.ok().build();
        } else
            return ResponseEntity.badRequest().body("No user id passed");
    }

    @GetMapping("/notifications/enable")
    @PreAuthorize("#{hasAnyRole('USER', 'ADMIN')}")
    @Override
    public ResponseEntity<?> enableNotification(HttpServletRequest request) {
        if (CookieUtils.getCookie(request, USER_ID).isEmpty()) {
            notificationService.enable(CookieUtils.getCookie(request, USER_ID).get());
            return ResponseEntity.ok().build();
        } else
            return ResponseEntity.badRequest().body("No user id passed");
    }

}
