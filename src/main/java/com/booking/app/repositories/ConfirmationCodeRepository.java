package com.booking.app.repositories;

import com.booking.app.entity.ConfirmationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConfirmationCodeRepository extends JpaRepository<ConfirmationCode, UUID> {

    ConfirmationCode findByCode(String code);

}
