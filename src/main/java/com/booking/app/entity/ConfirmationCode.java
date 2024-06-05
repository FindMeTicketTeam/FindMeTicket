package com.booking.app.entity;

import com.booking.app.util.TokenUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConfirmationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(mappedBy = "confirmationCode")
    private User user;

    @NotNull
    private String code;

    @NotNull
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date expiryTime;

    public static ConfirmationCode createCode() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutes = now.plusMinutes(10);
        Date dateExpiryTime = Date.from(tenMinutes.atZone(ZoneId.systemDefault()).toInstant());
        return ConfirmationCode.builder()
                .expiryTime(dateExpiryTime)
                .code(TokenUtils.generateRandomToken())
                .build();
    }

}
