package org.coffeeshop.stations.models;

import jakarta.persistence.*;

/** Entity representing a coffee shop station with opening hours. */
@Entity
@Table(name = "station")
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_id")
    private Long id;

    @Column(name = "station_name", nullable = false)
    private String name;

    @Column(name = "weekday_opening_hours")
    private String weekdayOpeningHours;

    @Column(name = "saturday_opening_hours")
    private String saturdayOpeningHours;

    @Column(name = "closed_on_sunday")
    private boolean closedOnSunday;

    /** No-arg constructor required by JPA. */
    public Station() {}

    /**
     * Creates a new Station with the given name and opening hours.
     *
     * @param name the station name
     * @param weekdayOpeningHours weekday hours in HH:mm-HH:mm format
     * @param saturdayOpeningHours saturday hours in HH:mm-HH:mm format
     * @param closedOnSunday whether the station is closed on Sundays
     */
    public Station(
            String name,
            String weekdayOpeningHours,
            String saturdayOpeningHours,
            boolean closedOnSunday) {
        this.name = name;
        this.weekdayOpeningHours = weekdayOpeningHours;
        this.saturdayOpeningHours = saturdayOpeningHours;
        this.closedOnSunday = closedOnSunday;
    }

    /**
     * Returns the station's database ID.
     *
     * @return the station ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the station's name.
     *
     * @return the station name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the weekday opening hours.
     *
     * @return weekday hours in HH:mm-HH:mm format
     */
    public String getWeekdayOpeningHours() {
        return weekdayOpeningHours;
    }

    /**
     * Returns the Saturday opening hours.
     *
     * @return Saturday hours in HH:mm-HH:mm format
     */
    public String getSaturdayOpeningHours() {
        return saturdayOpeningHours;
    }

    /**
     * Returns whether the station is closed on Sundays.
     *
     * @return true if closed on Sunday, false otherwise
     */
    public boolean isClosedOnSunday() {
        return closedOnSunday;
    }

    /**
     * Updates this station's schedule fields.
     *
     * @param weekdayHours weekday hours in HH:mm-HH:mm format
     * @param saturdayHours saturday hours in HH:mm-HH:mm format
     * @param closedOnSunday whether the station is closed on Sundays
     */
    public void updateSchedule(String weekdayHours, String saturdayHours, boolean closedOnSunday) {
        this.weekdayOpeningHours = weekdayHours;
        this.saturdayOpeningHours = saturdayHours;
        this.closedOnSunday = closedOnSunday;
    }
}
