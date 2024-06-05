package com.booking.app.services.impl;

import com.booking.app.entity.ConfirmationCode;
import com.booking.app.exception.exception.InvalidConfirmationCodeException;
import com.booking.app.repositories.ConfirmationCodeRepository;
import com.booking.app.services.ConfirmationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * This service class provides functionality for managing confirmation codes.
 */
@Service
@RequiredArgsConstructor
public class ConfirmationCodeServiceImpl implements ConfirmationCodeService {

    private final ConfirmationCodeRepository codeRepository;

    @Override
    public ConfirmationCode save(ConfirmationCode confirmationCode) {
        return codeRepository.save(confirmationCode);
    }

    @Override
    public void delete(ConfirmationCode confirmationCode) {
        codeRepository.delete(confirmationCode);
    }

    @Override
    public ConfirmationCode findByCode(String confirmationCode) {
        return codeRepository.findByCode(confirmationCode);
    }

    @Override
    public boolean verifyCode(ConfirmationCode confirmationCode, String givenCode) {
        LocalDateTime now = LocalDateTime.now();
        Date dateExpiryTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        if (dateExpiryTime.before(confirmationCode.getExpiryTime()) && confirmationCode.getCode().equals(givenCode)) {
            return true;
        }
        throw new InvalidConfirmationCodeException();
    }

    @Override
    public void updateConfirmationCode(ConfirmationCode newCode, ConfirmationCode existingCode) {
        existingCode.setCode(newCode.getCode());
        existingCode.setExpiryTime(newCode.getExpiryTime());
        save(existingCode);
    }

}
