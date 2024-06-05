package com.booking.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for representing a ticket.")
public class TicketDto {


    @Schema(description = "The unique identifier of the ticket.", example = "123e4567-e89b-12d3-a456-556642440000")
    private UUID id;

    @Schema(description = "The type of the ticket.", example = "Single")
    private String type;

    @Schema(description = "The place from where the journey starts.", example = "New York")
    private String placeFrom;

    @Schema(description = "The place at which the journey ends.", example = "Los Angeles")
    private String placeAt;

    @Schema(description = "The city of departure.", example = "New York")
    private String departureCity;

    @Schema(description = "The city of arrival.", example = "Los Angeles")
    private String arrivalCity;

    @Schema(description = "The time of departure.", example = "10:00 AM")
    private String departureTime;

    @Schema(description = "The date of departure.", example = "2024-06-01")
    private String departureDate;

    @Schema(description = "The time of arrival.", example = "03:00 PM")
    private String arrivalTime;

    @Schema(description = "The date of arrival.", example = "2024-06-01")
    private String arrivalDate;

    @Schema(description = "The total travel time.", example = "5h")
    private String travelTime;

    @Schema(description = "The price of the ticket.", example = "150.50")
    private BigDecimal price;

    @Schema(description = "The carrier of the ticket.", example = "Airline XYZ")
    private String carrier;

}
