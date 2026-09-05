package com.example.travel.notification.mapper;

import com.example.travel.notification.dto.NotificationResponse;
import com.example.travel.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
