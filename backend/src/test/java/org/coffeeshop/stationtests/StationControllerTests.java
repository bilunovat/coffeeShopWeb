package org.coffeeshop.stationtests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.coffeeshop.stations.dtos.UpdateStationDto;
import org.coffeeshop.stations.models.Station;
import org.coffeeshop.stations.repositories.StationRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for StationController. Uses Spring Boot's testing support with MockMvc to
 * perform integration tests on the station endpoints. Tests are grouped by operation using @Nested
 * classes and are transactional to avoid persisting test data. All tests run with ADMIN role
 * authentication via @WithMockUser.
 */
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class StationControllerTests {
    private static final Long FAULTY_ID = 999L;

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private StationRepository stationRepository;

    /**
     * Tests for GET /api/v1/station/{id} — station retrieval by ID scenarios including happy path
     * and 404 for missing stations.
     */
    @Nested
    class GetStationByIdTests {
        /**
         * Verifies that retrieving an existing station by ID returns 200 OK with the correct fields
         * including id, name, weekdayOpeningHours, saturdayOpeningHours, and closedOnSunday.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getStationById_returnsStation() throws Exception {
            Station station = saveTestStation();

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(station.getId()))
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(jsonPath("$.weekdayOpeningHours").value("08:00-18:00"))
                    .andExpect(jsonPath("$.saturdayOpeningHours").value("09:00-17:00"))
                    .andExpect(jsonPath("$.closedOnSunday").value(true));
        }

        /**
         * Verifies that retrieving a non-existent station ID returns 404 Not Found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getStationById_returns404ForMissingStation() throws Exception {
            mockMvc.perform(get("/api/v1/station/{id}", FAULTY_ID))
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * Tests for GET /api/v1/station — station list retrieval scenarios including happy path with
     * one or more stations.
     */
    @Nested
    class GetAllStationsTests {
        /**
         * Verifies that GET /api/v1/station returns 200 OK with a list containing a single
         * previously created station.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getAllStations_returnsListOfAllStationsWhenOne() throws Exception {
            clearAllStations();
            saveTestStation();

            mockMvc.perform(get("/api/v1/station"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("Leeds"));
        }

        /**
         * Verifies that GET /api/v1/station returns 200 OK with a list containing all previously
         * created stations.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getAllStations_returnsListOfAllStationsWhenMany() throws Exception {
            clearAllStations();
            saveTestStation();
            stationRepository.save(new Station("Newcastle", "07:30-16:30", "08:00-17:00", false));
            stationRepository.save(new Station("Durham", "09:00-18:00", "09:00-18:00", true));

            mockMvc.perform(get("/api/v1/station"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].name").value("Leeds"));
        }
    }

    /**
     * Tests for PUT /api/v1/station/{id} — station update scenarios including happy path, 404 for
     * missing station, and validation errors for null, empty, invalid, reversed, and identical
     * opening/closing hours.
     */
    @Nested
    class UpdateStationHoursTests {
        /**
         * Verifies that updating an existing station's opening hours with valid data returns 200
         * OK, and that a subsequent GET confirms the updated fields have been persisted while the
         * station name remains unchanged.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns200() throws Exception {
            Station station = saveTestStation();

            UpdateStationDto updateRequest =
                    new UpdateStationDto("04:00-18:00", "09:00-17:00", false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", station.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(jsonPath("$.weekdayOpeningHours").value("04:00-18:00"))
                    .andExpect(jsonPath("$.saturdayOpeningHours").value("09:00-17:00"))
                    .andExpect(jsonPath("$.closedOnSunday").value(false));
        }

        /**
         * Verifies that updating a non-existent station ID returns 404 Not Found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns404ForMissingStation() throws Exception {
            saveTestStation();

            UpdateStationDto updateRequest =
                    new UpdateStationDto("04:00-18:00", "09:00-17:00", false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", FAULTY_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        /**
         * Verifies that updating a station with a null weekday opening hours value returns 400 Bad
         * Request, as the @NotNull constraint on UpdateStationDto.weekdayOpeningHours rejects null
         * values, and that a subsequent GET confirms the station was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns400ForNullWeekdayHours() throws Exception {
            Station station = saveTestStation();

            UpdateStationDto updateRequest = new UpdateStationDto(null, "09:00-17:00", false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", station.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(
                            jsonPath("$.weekdayOpeningHours")
                                    .value(station.getWeekdayOpeningHours()))
                    .andExpect(
                            jsonPath("$.saturdayOpeningHours")
                                    .value(station.getSaturdayOpeningHours()))
                    .andExpect(jsonPath("$.closedOnSunday").value(station.isClosedOnSunday()));
        }

        /**
         * Verifies that updating a station with an empty weekday opening hours value returns 400
         * Bad Request, as the @NotBlank constraint on UpdateStationDto.weekdayOpeningHours rejects
         * empty strings, and that a subsequent GET confirms the station was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns400ForEmptyWeekdayHours() throws Exception {
            Station station = saveTestStation();

            UpdateStationDto updateRequest = new UpdateStationDto("", "09:00-17:00", false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", station.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(
                            jsonPath("$.weekdayOpeningHours")
                                    .value(station.getWeekdayOpeningHours()))
                    .andExpect(
                            jsonPath("$.saturdayOpeningHours")
                                    .value(station.getSaturdayOpeningHours()))
                    .andExpect(jsonPath("$.closedOnSunday").value(station.isClosedOnSunday()));
        }

        /**
         * Verifies that updating a station with a null saturday opening hours value returns 400 Bad
         * Request, as the @NotNull constraint on UpdateStationDto.saturdayOpeningHours rejects null
         * values, and that a subsequent GET confirms the station was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns400ForNullWeekendHours() throws Exception {
            Station station = saveTestStation();

            UpdateStationDto updateRequest = new UpdateStationDto("04:00-18:00", null, false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", station.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(
                            jsonPath("$.weekdayOpeningHours")
                                    .value(station.getWeekdayOpeningHours()))
                    .andExpect(
                            jsonPath("$.saturdayOpeningHours")
                                    .value(station.getSaturdayOpeningHours()))
                    .andExpect(jsonPath("$.closedOnSunday").value(station.isClosedOnSunday()));
        }

        /**
         * Verifies that updating a station with an empty saturday opening hours value returns 400
         * Bad Request, as the @NotBlank constraint on UpdateStationDto.saturdayOpeningHours rejects
         * empty strings, and that a subsequent GET confirms the station was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns400ForEmptyWeekendHours() throws Exception {
            Station station = saveTestStation();

            UpdateStationDto updateRequest = new UpdateStationDto("04:00-18:00", "", false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", station.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(
                            jsonPath("$.weekdayOpeningHours")
                                    .value(station.getWeekdayOpeningHours()))
                    .andExpect(
                            jsonPath("$.saturdayOpeningHours")
                                    .value(station.getSaturdayOpeningHours()))
                    .andExpect(jsonPath("$.closedOnSunday").value(station.isClosedOnSunday()));
        }

        /**
         * Verifies that updating a station with reversed weekday hours (closing time before opening
         * time) returns 400 Bad Request, and that a subsequent GET confirms the station was not
         * modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns400WhenReversedWeekdayHours() throws Exception {
            Station station = saveTestStation();

            UpdateStationDto updateRequest =
                    new UpdateStationDto("18:00-04:00", "09:00-17:00", false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", station.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(
                            jsonPath("$.weekdayOpeningHours")
                                    .value(station.getWeekdayOpeningHours()))
                    .andExpect(
                            jsonPath("$.saturdayOpeningHours")
                                    .value(station.getSaturdayOpeningHours()))
                    .andExpect(jsonPath("$.closedOnSunday").value(station.isClosedOnSunday()));
        }

        /**
         * Verifies that updating a station with reversed saturday hours (closing time before
         * opening time) returns 400 Bad Request, and that a subsequent GET confirms the station was
         * not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns400WhenReversedWeekendHours() throws Exception {
            Station station = saveTestStation();

            UpdateStationDto updateRequest =
                    new UpdateStationDto("04:00-18:00", "17:00-09:00", false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", station.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(
                            jsonPath("$.weekdayOpeningHours")
                                    .value(station.getWeekdayOpeningHours()))
                    .andExpect(
                            jsonPath("$.saturdayOpeningHours")
                                    .value(station.getSaturdayOpeningHours()))
                    .andExpect(jsonPath("$.closedOnSunday").value(station.isClosedOnSunday()));
        }

        /**
         * Verifies that updating a station with an invalid weekday hours value ("25:00" exceeds the
         * 24-hour clock) returns 400 Bad Request, as the @Pattern constraint on UpdateStationDto
         * rejects invalid time formats, and that a subsequent GET confirms the station was not
         * modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns400WhenInvalidWeekdayHours() throws Exception {
            Station station = saveTestStation();

            UpdateStationDto updateRequest =
                    new UpdateStationDto("04:00-25:00", "09:00-17:00", false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", station.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(
                            jsonPath("$.weekdayOpeningHours")
                                    .value(station.getWeekdayOpeningHours()))
                    .andExpect(
                            jsonPath("$.saturdayOpeningHours")
                                    .value(station.getSaturdayOpeningHours()))
                    .andExpect(jsonPath("$.closedOnSunday").value(station.isClosedOnSunday()));
        }

        /**
         * Verifies that updating a station with an invalid saturday hours value ("25:00" exceeds
         * the 24-hour clock) returns 400 Bad Request, as the @Pattern constraint on
         * UpdateStationDto rejects invalid time formats, and that a subsequent GET confirms the
         * station was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns400WhenInvalidWeekendHours() throws Exception {
            Station station = saveTestStation();

            UpdateStationDto updateRequest =
                    new UpdateStationDto("04:00-18:00", "09:00-25:00", false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", station.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(
                            jsonPath("$.weekdayOpeningHours")
                                    .value(station.getWeekdayOpeningHours()))
                    .andExpect(
                            jsonPath("$.saturdayOpeningHours")
                                    .value(station.getSaturdayOpeningHours()))
                    .andExpect(jsonPath("$.closedOnSunday").value(station.isClosedOnSunday()));
        }

        /**
         * Verifies that updating a station with identical opening and closing hours (e.g.,
         * "04:00-04:00") returns 400 Bad Request, as the service rejects zero-length opening
         * windows, and that a subsequent GET confirms the station was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStation_returns400WhenSameOpeningClosingHours() throws Exception {
            Station station = saveTestStation();

            UpdateStationDto updateRequest =
                    new UpdateStationDto("04:00-04:00", "17:00-09:00", false);

            mockMvc.perform(
                            put("/api/v1/station/{id}", station.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/station/{id}", station.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Leeds"))
                    .andExpect(
                            jsonPath("$.weekdayOpeningHours")
                                    .value(station.getWeekdayOpeningHours()))
                    .andExpect(
                            jsonPath("$.saturdayOpeningHours")
                                    .value(station.getSaturdayOpeningHours()))
                    .andExpect(jsonPath("$.closedOnSunday").value(station.isClosedOnSunday()));
        }
    }

    /**
     * Creates and persists a test Station entity with predefined opening hours, then flushes and
     * clears the persistence context to ensure fresh loading.
     *
     * @return the saved Station entity
     */
    private Station saveTestStation() {
        Station station =
                stationRepository.save(new Station("Leeds", "08:00-18:00", "09:00-17:00", true));

        entityManager.flush();
        entityManager.clear();

        return station;
    }

    /**
     * Removes all station records from the database and clears the persistence context. Used to
     * ensure a clean slate before count-dependent tests.
     */
    private void clearAllStations() {
        stationRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }
}
