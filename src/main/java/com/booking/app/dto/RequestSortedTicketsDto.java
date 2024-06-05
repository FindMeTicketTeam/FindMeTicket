package com.booking.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Schema(description = "Data Transfer Object for requesting sorted tickets.")
public class RequestSortedTicketsDto {

    @Schema(description = "City of departure", example = "New York")
    private String departureCity;

    @Schema(description = "City of arrival", example = "Los Angeles")
    private String arrivalCity;

    @Schema(description = "Date of departure in the format dd.MM.yyyy", example = "01.06.2024")
    private String departureDate;

    @Schema(description = "Sorting criteria", example = "price")
    private String sortingBy;

    @Schema(description = "Indicates if the sorting is ascending", example = "true")
    private boolean ascending;

    @Schema(description = "Indicates if the travel is by bus", example = "true")
    private Boolean bus;

    @Schema(description = "Indicates if the travel is by train", example = "true")
    private Boolean train;

    @Schema(description = "Indicates if the travel is by airplane", example = "false")
    private Boolean airplane;

    @Schema(description = "Indicates if the travel is by ferry", example = "false")
    private Boolean ferry;
}
