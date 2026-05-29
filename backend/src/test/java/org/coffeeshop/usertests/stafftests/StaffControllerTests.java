package org.coffeeshop.usertests.stafftests;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.coffeeshop.users.dtos.CreateStaffDto;
import org.coffeeshop.users.dtos.UpdateStaffDto;
import org.coffeeshop.users.models.Staff;
import org.coffeeshop.users.repositories.StaffRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for StaffController. Uses Spring Boot's testing support with MockMvc to perform
 * integration tests on the staff endpoints. Tests are grouped by operation using @Nested classes
 * and are transactional to avoid persisting test data. All tests run with ADMIN role authentication
 * via @WithMockUser.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class StaffControllerTests {
    private static final Long FAULTY_ID = 999L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EntityManager entityManager;

    @Autowired private StaffRepository staffRepository;

    /**
     * Tests for POST /api/v1/staff — staff creation scenarios including happy path, validation
     * errors for null, empty, and invalid fields, and duplicate username constraint.
     */
    @Nested
    class CreateStaffTests {
        /**
         * Verifies that creating a staff member with valid data returns 201 Created with the
         * correct fields including id, username, firstName, lastName, and role. Also asserts the
         * staff member is persisted in the repository.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returnsCreatedStaff() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com",
                            "Davina",
                            "Odili",
                            "staff_user",
                            "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.username").value("staff.create@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Davina"))
                    .andExpect(jsonPath("$.lastName").value("Odili"))
                    .andExpect(jsonPath("$.role").value("staff_user"));

            assertTrue(staffRepository.existsByUsername("staff.create@example.com"));
        }

        /**
         * Verifies that creating a staff member with a null username returns 400 Bad Request, as
         * the @NotBlank constraint on CreateStaffDto.username rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenUsernameIsNull() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(null, "Davina", "Odili", "staff_user", "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with an empty username returns 400 Bad Request, as
         * the @NotBlank constraint on CreateStaffDto.username rejects empty strings.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenUsernameIsEmpty() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto("", "Davina", "Odili", "staff_user", "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with a username that is too short returns 400 Bad
         * Request, as the @Size(min = 3) constraint on CreateStaffDto.username rejects short
         * values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenUsernameIsTooShort() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto("a", "Davina", "Odili", "staff_user", "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with a null first name returns 400 Bad Request, as
         * the @NotBlank constraint on CreateStaffDto.firstName rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenFirstNameIsNull() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com", null, "Odili", "staff_user", "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with an empty first name returns 400 Bad Request,
         * as the @NotBlank constraint on CreateStaffDto.firstName rejects empty strings.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenFirstNameIsEmpty() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com", "", "Odili", "staff_user", "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with a null last name returns 400 Bad Request, as
         * the @NotBlank constraint on CreateStaffDto.lastName rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenLastNameIsNull() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com", "Davina", null, "staff_user", "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with an empty last name returns 400 Bad Request, as
         * the @NotBlank constraint on CreateStaffDto.lastName rejects empty strings.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenLastNameIsEmpty() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com", "Davina", "", "staff_user", "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with an empty role returns 400 Bad Request, as
         * the @NotBlank constraint on CreateStaffDto.role rejects empty strings.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenRoleIsEmpty() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com", "Davina", "Odili", "", "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with a null role returns 400 Bad Request, as
         * the @NotBlank constraint on CreateStaffDto.role rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenRoleIsNull() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com", "Davina", "Odili", null, "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with an empty password returns 400 Bad Request, as
         * the @NotBlank constraint on CreateStaffDto.password rejects empty strings.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenPasswordIsEmpty() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com", "Davina", "Odili", "staff_user", "");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with a null password returns 400 Bad Request, as
         * the @NotBlank constraint on CreateStaffDto.password rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenPasswordIsNull() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com", "Davina", "Odili", "staff_user", null);

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with a password that is too short returns 400 Bad
         * Request, as the @Size(min = 6) constraint on CreateStaffDto.password rejects short
         * values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400WhenPasswordIsTooShort() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com", "Davina", "Odili", "staff_user", "123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that creating a staff member with a username that already exists returns 400 Bad
         * Request, as the unique constraint on Staff.username prevents duplicate usernames.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createStaff_returns400ForSameUsername() throws Exception {
            CreateStaffDto request =
                    new CreateStaffDto(
                            "staff.create@example.com",
                            "Davina",
                            "Odili",
                            "staff_user",
                            "secret123");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            CreateStaffDto sameUsername =
                    new CreateStaffDto(
                            "staff.create@example.com", "John", "Smith", "staff_user", "secret111");

            mockMvc.perform(
                            post("/api/v1/staff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(sameUsername)))
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * Tests for GET /api/v1/staff — staff retrieval scenarios including get by ID, get all, and 404
     * for missing staff.
     */
    @Nested
    class GetStaffTests {
        /**
         * Verifies that retrieving an existing staff member by ID returns 200 OK with the correct
         * fields including id, username, firstName, lastName, and role.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getStaffById_returnsStaff() throws Exception {
            Staff savedStaff =
                    staffRepository.save(
                            new Staff(
                                    "barista1@example.com",
                                    "Alex",
                                    "Brown",
                                    "staff_user",
                                    "encoded-password"));

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("barista1@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that retrieving a non-existent staff ID returns 404 Not Found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getStaffById_returns404ForMissingStaff() throws Exception {
            mockMvc.perform(get("/api/v1/staff/{id}", FAULTY_ID)).andExpect(status().isNotFound());

            assertEquals(0, staffRepository.count());
        }

        /**
         * Verifies that GET /api/v1/staff returns 200 OK with a list containing all previously
         * created staff members.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getAllStaff_returnsListOfAllStaff() throws Exception {
            staffRepository.save(
                    new Staff(
                            "staff.one@example.com",
                            "Alex",
                            "Brown",
                            "staff_user",
                            "encoded-password-1"));
            staffRepository.save(
                    new Staff(
                            "staff.two@example.com",
                            "Jamie",
                            "Smith",
                            "Admin",
                            "encoded-password-2"));

            mockMvc.perform(get("/api/v1/staff"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(
                            jsonPath(
                                    "$[*].username",
                                    containsInAnyOrder(
                                            "staff.one@example.com", "staff.two@example.com")));
        }

        /**
         * Verifies that GET /api/v1/staff returns 200 OK with an empty list when no staff members
         * exist.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getAllStaff_returnsEmptyListWhenNoStaff() throws Exception {
            mockMvc.perform(get("/api/v1/staff"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    /**
     * Tests for PUT /api/v1/staff/{id} — staff update scenarios including happy path, 404 for
     * missing staff, validation errors, and path id authority.
     */
    @Nested
    class UpdateStaffTests {
        /**
         * Verifies that updating an existing staff member with valid data returns 200 OK, and that
         * a subsequent GET confirms the updated fields have been persisted.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns200() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto(
                            "staff.updated@example.com",
                            "John",
                            "Umunna",
                            "Admin",
                            "newpassword123");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff.updated@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Umunna"))
                    .andExpect(jsonPath("$.role").value("Admin"));

            assertEquals(1, staffRepository.count());
        }

        /**
         * Verifies that updating a non-existent staff ID returns 404 Not Found, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns404ForMissingStaff() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto(
                            "staff.updated@example.com",
                            "John",
                            "Umunna",
                            "Admin",
                            "newpassword123");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", FAULTY_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with a null first name returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateStaffDto.firstName rejects null values, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns400ForNullFirstName() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto(
                            "staff.updated@example.com", null, "Umunna", "Admin", "newpassword123");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with an empty first name returns 400 Bad Request,
         * as the @NotBlank constraint on UpdateStaffDto.firstName rejects empty strings, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns400ForEmptyFirstName() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto(
                            "staff.updated@example.com", "", "Umunna", "Admin", "newpassword123");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with a null last name returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateStaffDto.lastName rejects null values, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns400ForNullLastName() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto(
                            "staff.updated@example.com", "John", null, "Admin", "newpassword123");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with an empty last name returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateStaffDto.lastName rejects empty strings, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns400ForEmptyLastName() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto(
                            "staff.updated@example.com", "John", "", "Admin", "newpassword123");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with a null role returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateStaffDto.role rejects null values, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns400ForNullRole() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto(
                            "staff.updated@example.com", "John", "Umunna", null, "newpassword123");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with an empty role returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateStaffDto.role rejects empty strings, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns400ForEmptyRole() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto(
                            "staff.updated@example.com", "John", "Umunna", "", "newpassword123");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with a null username returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateStaffDto.username rejects null values, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns400ForNullUsername() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto(null, "John", "Umunna", "Admin", "newpassword123");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with an empty username returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateStaffDto.username rejects empty strings, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns400ForEmptyUsername() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto("", "John", "Umunna", "Admin", "newpassword123");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with a null password returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateStaffDto.password rejects null values, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns400ForNullPassword() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto(
                            "staff.updated@example.com", "John", "Umunna", "Admin", null);

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with an empty password returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateStaffDto.password rejects empty strings, and that a
         * subsequent GET confirms the existing staff member was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns400ForEmptyPassword() throws Exception {
            Staff savedStaff = saveTestStaff();
            UpdateStaffDto updatedStaff =
                    new UpdateStaffDto("staff.updated@example.com", "John", "Umunna", "Admin", "");

            mockMvc.perform(
                            put("/api/v1/staff/{id}", savedStaff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStaff)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedStaff.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Alex"))
                    .andExpect(jsonPath("$.lastName").value("Brown"))
                    .andExpect(jsonPath("$.role").value("staff_user"));
        }

        /**
         * Verifies that updating a staff member with a different id in the request body than the
         * path id returns 200 OK and that the path id remains authoritative, updating only the
         * staff record identified by the path id.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateStaff_returns200WhenBodyIdDiffersFromPathId() throws Exception {
            Staff target = saveTestStaff();
            Staff other =
                    staffRepository.save(
                            new Staff(
                                    "staff.other@example.com",
                                    "Other",
                                    "User",
                                    "staff_user",
                                    "encoded-password"));

            UpdateStaffDto bodyWithDifferentId =
                    new UpdateStaffDto(
                            "staff@example.com", "Updated", "Name", "Admin", "newpassword123");

            String json = objectMapper.writeValueAsString(bodyWithDifferentId);
            mockMvc.perform(
                            put("/api/v1/staff/{id}", target.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(target.getId()))
                    .andExpect(jsonPath("$.username").value("staff@example.com"));

            assertTrue(
                    staffRepository
                            .findById(target.getId())
                            .map(staff -> "staff@example.com".equals(staff.getUsername()))
                            .orElse(false));
            assertTrue(
                    staffRepository
                            .findById(other.getId())
                            .map(staff -> "staff.other@example.com".equals(staff.getUsername()))
                            .orElse(false));
        }
    }

    /**
     * Tests for DELETE /api/v1/staff/{id} — staff deletion scenarios including happy path with
     * follow-up verification and 404 for missing staff.
     */
    @Nested
    class DeleteStaffTests {
        /**
         * Verifies that deleting an existing staff member returns 204 No Content, and that a
         * subsequent GET returns 404. Also asserts that the staff member no longer exists in the
         * repository.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void deleteStaff_returns204() throws Exception {
            Staff savedStaff = saveTestStaff();

            mockMvc.perform(delete("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/staff/{id}", savedStaff.getId()))
                    .andExpect(status().isNotFound());

            assertFalse(staffRepository.existsById(savedStaff.getId()));
        }

        /**
         * Verifies that deleting a non-existent staff ID returns 404 Not Found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void deleteStaff_returns404ForMissingStaff() throws Exception {
            mockMvc.perform(delete("/api/v1/staff/{id}", FAULTY_ID))
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * Creates and persists a test Staff entity with predefined data, then flushes and clears the
     * persistence context to ensure fresh loading.
     *
     * @return the saved Staff entity
     */
    private Staff saveTestStaff() {
        Staff staff =
                staffRepository.save(
                        new Staff(
                                "staff@example.com",
                                "Alex",
                                "Brown",
                                "staff_user",
                                "encoded-password"));

        entityManager.flush();
        entityManager.clear();

        return staff;
    }
}
