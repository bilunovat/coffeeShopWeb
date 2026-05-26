package org.coffeeshop.stations.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for updating a station's opening hours. Validates that opening hours follow the HH:mm-HH:mm
 * 24-hour format and that all required fields are present and non-blank.
 */
public record UpdateStationDto(
        @NotBlank
                @NotNull
                @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d-([01]\\d|2[0-3]):[0-5]\\d$")
                String weekdayOpeningHours,
        @NotBlank
                @NotNull
                @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d-([01]\\d|2[0-3]):[0-5]\\d$")
                String saturdayOpeningHours,
        boolean closedOnSunday) {}
