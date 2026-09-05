package com.example.travel.notification.service;

import com.example.travel.common.dto.PageResponse;
import com.example.travel.common.event.DomainEventPublisher;
import com.example.travel.common.exception.ResourceNotFoundException;
import com.example.travel.notification.dto.NotificationResponse;
import com.example.travel.notification.entity.Notification;
import com.example.travel.notification.entity.NotificationType;
import com.example.travel.notification.event.NotificationCreatedEvent;
import com.example.travel.notification.mapper.NotificationMapper;
import com.example.travel.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists notifications synchronously (see docs/architecture/decisions.md and plan.md 13). A
 * future phase can move creation onto {@link DomainEventPublisher} consumers without this
 * service's public contract changing.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public void create(Long userId, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification = notificationRepository.save(notification);
        eventPublisher.publish(NotificationCreatedEvent.now(notification.getId(), userId));
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listForUser(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findAllByUserId(userId, pageable);
        return PageResponse.of(page, notificationMapper::toResponse);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", notificationId));
        notification.setRead(true);
        return notificationMapper.toResponse(notification);
    }
}
