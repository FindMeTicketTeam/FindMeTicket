package com.booking.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Details about city")
public class CityDto {
    @Schema(description = "Country name", example = "Ukraine")
    private String country;

    @Schema(description = "City name in Ukrainian", example = "Київ")
    private String cityUa;

    @Schema(description = "City name in English", example = "Kyiv")
    private String cityEng;

    @Schema(description = "Content language", example = "en")
    private String language;
}
