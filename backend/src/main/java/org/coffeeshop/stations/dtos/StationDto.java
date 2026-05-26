package org.coffeeshop.stations.dtos;

/** DTO representing a coffee shop station's data. */
public record StationDto(
        Long id,
        String name,
        String weekdayOpeningHours,
        String saturdayOpeningHours,
        boolean closedOnSunday) {}
