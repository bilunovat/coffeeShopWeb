package org.coffeeshop.users.services;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.coffeeshop.exceptions.userexceptions.StaffServiceException;
import org.coffeeshop.users.dtos.CreateStaffDto;
import org.coffeeshop.users.dtos.StaffDto;
import org.coffeeshop.users.dtos.UpdateStaffDto;
import org.coffeeshop.users.models.Staff;
import org.coffeeshop.users.repositories.StaffRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Service for managing staff users in the coffee shop. */
@Service
public class StaffService {
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new StaffService instance.
     *
     * @param staffRepository the repository for staff database operations
     * @param passwordEncoder the encoder used to hash staff passwords
     */
    public StaffService(StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new staff member from the provided DTO. Encodes the password before persisting and
     * ensures the username is unique.
     *
     * @param dto the staff creation data
     * @return the created staff member as a DTO
     * @throws StaffServiceException if a staff member with the same username already exists
     */
    public StaffDto create(CreateStaffDto dto) {
        Staff staff = fromCreateDto(dto);
        if (staffRepository.existsByUsername(staff.getUsername())) {
            throw new StaffServiceException(
                    "Staff with username " + staff.getUsername() + " already exists");
        }
        Staff saved = staffRepository.save(staff);
        return toDto(saved);
    }

    /**
     * Retrieves a staff member by ID.
     *
     * @param id the staff user ID
     * @return the corresponding staff DTO
     * @throws EntityNotFoundException if the staff user cannot be found
     */
    public StaffDto getStaffById(Long id) {
        Staff staff =
                staffRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));
        return toDto(staff);
    }

    /**
     * Retrieves all staff members from the database.
     *
     * @return an unmodifiable list of all staff members as DTOs
     */
    public List<StaffDto> getAllStaff() {
        List<Staff> allStaff = staffRepository.findAll();
        return toDto(allStaff);
    }

    /**
     * Updates an existing staff member identified by the given ID. Encodes the password before
     * persisting and ensures the new username is unique.
     *
     * @param id the path ID identifying the staff record to update
     * @param dto the updated staff data
     * @return the updated staff member as a DTO
     * @throws EntityNotFoundException if no staff member with the given ID exists
     * @throws StaffServiceException if the new username is already taken by another staff member
     */
    public StaffDto updateStaff(Long id, UpdateStaffDto dto) {
        Staff existing =
                staffRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));

        if (!existing.getUsername().equals(dto.username())
                && staffRepository.existsByUsername(dto.username())) {
            throw new StaffServiceException(
                    "Staff with username " + dto.username() + " already exists");
        }

        Staff updatedStaff =
                new Staff(
                        existing.getId(),
                        dto.username(),
                        dto.firstName(),
                        dto.lastName(),
                        dto.role(),
                        passwordEncoder.encode(dto.password()));
        Staff updated = staffRepository.save(updatedStaff);
        return toDto(updated);
    }

    /**
     * Deletes a staff member by ID.
     *
     * @param id the ID of the staff member to delete
     * @throws EntityNotFoundException if no staff member with the given ID exists
     */
    public void deleteStaff(Long id) {
        Staff entity =
                staffRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));

        staffRepository.delete(entity);
    }

    /** Internal lookup returning the entity (for use by other services). */
    public Staff getStaffEntityById(Long id) {
        return staffRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));
    }

    /**
     * Converts a Staff entity to a StaffDto. The password is write-only and never returned to
     * clients.
     *
     * @param staff the staff entity to convert
     * @return the corresponding StaffDto with a null password field
     */
    private StaffDto toDto(Staff staff) {
        return new StaffDto(
                staff.getId(),
                staff.getUsername(),
                staff.getFirstName(),
                staff.getLastName(),
                staff.getRole(),
                null);
    }

    /**
     * Converts a CreateStaffDto into a Staff entity, encoding the password before construction.
     *
     * @param dto the creation DTO to convert
     * @return a new Staff entity with an encoded password
     */
    private Staff fromCreateDto(CreateStaffDto dto) {
        return new Staff(
                dto.username(),
                dto.firstName(),
                dto.lastName(),
                dto.role(),
                passwordEncoder.encode(dto.password()));
    }

    /**
     * Converts a list of Staff entities to a list of StaffDto objects.
     *
     * @param staff the list of staff entities to convert
     * @return a list of corresponding StaffDto objects
     */
    private List<StaffDto> toDto(List<Staff> staff) {
        List<StaffDto> dtos = new ArrayList<>();
        for (Staff staffMember : staff) {
            dtos.add(toDto(staffMember));
        }
        return dtos;
    }
}
