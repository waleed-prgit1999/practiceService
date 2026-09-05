package com.example.travel.user.service;

import com.example.travel.audit.entity.AuditAction;
import com.example.travel.audit.service.AuditService;
import com.example.travel.common.dto.PageResponse;
import com.example.travel.common.exception.ConflictException;
import com.example.travel.common.event.DomainEventPublisher;
import com.example.travel.common.exception.ResourceNotFoundException;
import com.example.travel.auth.event.UserRegisteredEvent;
import com.example.travel.user.config.UserProvisioningProperties;
import com.example.travel.user.dto.AdminCreateUserRequest;
import com.example.travel.user.dto.UpdateUserRequest;
import com.example.travel.user.dto.UserResponse;
import com.example.travel.user.entity.Role;
import com.example.travel.user.entity.RoleName;
import com.example.travel.user.entity.User;
import com.example.travel.user.entity.UserStatus;
import com.example.travel.user.mapper.UserMapper;
import com.example.travel.user.repository.RoleRepository;
import com.example.travel.user.repository.UserRepository;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher eventPublisher;
    private final UserProvisioningProperties provisioningProperties;

    @Transactional
    public User registerNewUser(String firstName, String lastName, String email, String passwordHash, String phone) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered: " + normalizedEmail);
        }
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("Required reference role USER is missing"));

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordHash);
        user.setPhone(phone);
        user.setStatus(UserStatus.ACTIVE);
        user.getRoles().add(userRole);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return userMapper.toResponse(getEntityById(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateUserRequest request) {
        User user = getEntityById(userId);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> adminListUsers(UserStatus statusFilter, Pageable pageable) {
        Page<User> page = statusFilter != null
                ? userRepository.findAllByStatus(statusFilter, pageable)
                : userRepository.findAll(pageable);
        return PageResponse.of(page, userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse adminGetUser(Long id) {
        return userMapper.toResponse(getEntityById(id));
    }

    /**
     * Creates an ACTIVE user with the USER role and the configured default password. Reuses
     * {@link #registerNewUser} so the duplicate-email check and role assignment stay in one place;
     * that also means this endpoint cannot create administrators.
     *
     * <p>The password is never returned — see plan.md 5.1. Whoever runs this is expected to know
     * the configured default and communicate it out of band.
     */
    @Transactional
    public UserResponse adminCreateUser(AdminCreateUserRequest request, Long actingAdminId) {
        String passwordHash = passwordEncoder.encode(provisioningProperties.defaultNewUserPassword());
        User user = registerNewUser(
                request.firstName(), request.lastName(), request.email(), passwordHash, request.phone());

        auditService.record(
                actingAdminId,
                AuditAction.USER_CREATED_BY_ADMIN,
                "User",
                user.getId(),
                Map.of("email", user.getEmail()));
        eventPublisher.publish(UserRegisteredEvent.now(user.getId(), user.getEmail()));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse adminUpdateStatus(Long id, UserStatus newStatus, Long actingAdminId) {
        User user = getEntityById(id);
        user.setStatus(newStatus);
        auditService.record(
                actingAdminId, AuditAction.USER_STATUS_CHANGED, "User", id, Map.of("newStatus", newStatus.name()));
        return userMapper.toResponse(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
