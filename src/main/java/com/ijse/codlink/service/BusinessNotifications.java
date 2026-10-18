package com.ijse.codlink.service;

import com.ijse.codlink.entity.*;
import com.ijse.codlink.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessNotifications {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void customer(Customer customer, String message) {
        if (customer != null && customer.getUser() != null) send(customer.getUser(), message);
    }

    @Transactional
    public void send(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }
}
