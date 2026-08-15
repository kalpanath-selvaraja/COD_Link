package com.ijse.codlink.service;

import com.ijse.codlink.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
    NotificationDTO createNotification(NotificationDTO dto);

    List<NotificationDTO> getByUserId(Long userId);
}
