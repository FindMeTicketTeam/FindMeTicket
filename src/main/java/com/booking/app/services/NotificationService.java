package com.booking.app.services;

import jakarta.servlet.http.Cookie;

public interface NotificationService {
    void disable(Cookie id);

    void enable(Cookie id);
}
