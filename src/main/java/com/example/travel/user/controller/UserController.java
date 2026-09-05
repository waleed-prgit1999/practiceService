package com.example.travel.user.controller;

import com.example.travel.auth.security.UserPrincipal;
import com.example.travel.user.dto.UpdateUserRequest;
import com.example.travel.user.dto.UserResponse;
import com.example.travel.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Tag(name = "Current User", description = "View and update the authenticated user's own profile.")
public class UserController {

    private final UserService userService;

    @GetMapping
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getProfile(principal.getId());
    }

    @PutMapping
    public UserResponse updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateProfile(principal.getId(), request);
    }
}
