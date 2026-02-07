package com.project.FoodieHub.email_notification.services;

import com.project.FoodieHub.email_notification.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO);
}
