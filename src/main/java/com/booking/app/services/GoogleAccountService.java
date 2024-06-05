package com.booking.app.services;

import com.booking.app.dto.SocialLoginDto;
import com.booking.app.entity.User;

import java.util.Optional;

public interface GoogleAccountService {
    Optional<User> loginOAuthGoogle(SocialLoginDto requestBody);
}
