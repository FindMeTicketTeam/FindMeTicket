package com.booking.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for saving reviews, including the review text and the grade.")
public class SaveReviewDto {

    @NotNull
    @Schema(example = "The best service.", description = "The text of the review.", required = true)
    private String reviewText;

    @NotNull
    @Schema(description = "The grade given for the review.", required = true, example = "5")
    private int grade;
}
