package com.booking.app.services.impl;

import com.booking.app.dto.PasswordDto;
import com.booking.app.dto.RequestUpdatePasswordDTO;
import com.booking.app.entity.User;
import com.booking.app.exception.exception.LastPasswordIsNotRightException;
import com.booking.app.exception.exception.UserNotFoundException;
import com.booking.app.services.ConfirmationCodeService;
import com.booking.app.services.MailSenderService;
import com.booking.app.services.PasswordService;
import com.booking.app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link PasswordService} for managing password-related operations.
 * This service handles sending reset codes, resetting passwords, and changing passwords.
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class PasswordServiceImpl implements PasswordService {

    private final MailSenderService mailSenderService;
    private final UserService userService;
    private final ConfirmationCodeService confirmationCodeService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void resetPassword(PasswordDto dto) {
        userService.findByEmail(dto.getEmail())
                .ifPresentOrElse(user -> {
                    if (userService.isEnabled(user)
                            && confirmationCodeService.verifyCode(user.getConfirmationCode(), dto.getCode())) {
                        userService.updatePassword(user.getId(), passwordEncoder.encode(dto.getPassword()));
                    }
                }, () -> {
                    throw new UserNotFoundException();
                });

    }

    @Override
    public void changePassword(RequestUpdatePasswordDTO dto, User user) {
        String currentPassword = user.getPassword();
        String lastPassword = dto.getLastPassword();
        if (passwordEncoder.matches(lastPassword, currentPassword)) {
            userService.updatePassword(user.getId(), passwordEncoder.encode(dto.getPassword()));
        } else {
            throw new LastPasswordIsNotRightException();
        }
    }

}
