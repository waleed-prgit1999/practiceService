package com.example.travel.user.controller;

import com.example.travel.auth.security.UserPrincipal;
import com.example.travel.common.dto.PageResponse;
import com.example.travel.user.dto.AdminCreateUserRequest;
import com.example.travel.user.dto.UserResponse;
import com.example.travel.user.dto.UserStatusUpdateRequest;
import com.example.travel.user.entity.UserStatus;
import com.example.travel.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "Admin-only user management: list, view and enable/disable users.")
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            UriComponentsBuilder uriBuilder) {
        UserResponse created = userService.adminCreateUser(request, principal.getId());
        URI location = uriBuilder.path("/api/v1/users/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public PageResponse<UserResponse> listUsers(
            @RequestParam(required = false) UserStatus status, Pageable pageable) {
        return userService.adminListUsers(status, pageable);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.adminGetUser(id);
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return userService.adminUpdateStatus(id, request.status(), principal.getId());
    }
}
