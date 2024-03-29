package com.booking.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TrainComfortInfoDTO {

    private String url;

    private String comfort;

    private BigDecimal price;
}
