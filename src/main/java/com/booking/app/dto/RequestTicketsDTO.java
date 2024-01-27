package com.booking.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestTicketsDTO {
    private String departureCity;
    private String arrivalCity;
    private String departureDate;
    private Boolean bus;
    private Boolean train;
    private Boolean airplane;
    private Boolean ferry;
}
