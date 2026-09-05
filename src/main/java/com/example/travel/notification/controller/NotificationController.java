package com.example.travel.notification.controller;

import com.example.travel.auth.security.UserPrincipal;
import com.example.travel.common.dto.PageResponse;
import com.example.travel.notification.dto.NotificationResponse;
import com.example.travel.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "View and acknowledge the authenticated user's notifications.")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public PageResponse<NotificationResponse> listNotifications(
            @AuthenticationPrincipal UserPrincipal principal, Pageable pageable) {
        return notificationService.listForUser(principal.getId(), pageable);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return notificationService.markAsRead(id, principal.getId());
    }
}
