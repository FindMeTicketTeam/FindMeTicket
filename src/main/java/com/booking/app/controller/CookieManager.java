package com.booking.app.controller;

import com.booking.app.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.booking.app.constant.CustomHttpHeaders.REMEMBER_ME;
import static com.booking.app.constant.CustomHttpHeaders.USER_ID;
import static com.booking.app.constant.JwtTokenConstants.REFRESH_TOKEN;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieManager {

    /**
     * Deletes cookies related to user authentication.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     */
    public static void deleteCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, REFRESH_TOKEN);
        CookieUtils.deleteCookie(request, response, USER_ID);
        CookieUtils.deleteCookie(request, response, REMEMBER_ME);
    }

}
