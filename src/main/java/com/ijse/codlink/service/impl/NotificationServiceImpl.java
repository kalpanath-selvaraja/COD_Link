package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.NotificationDTO;
import com.ijse.codlink.entity.Notification;
import com.ijse.codlink.entity.User;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.NotificationRepository;
import com.ijse.codlink.repository.UserRepository;
import com.ijse.codlink.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@org.springframework.transaction.annotation.Transactional
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final com.ijse.codlink.security.RecordAccess recordAccess;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository, com.ijse.codlink.security.RecordAccess recordAccess) {
        this.recordAccess = recordAccess;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }


    @Override
    public NotificationDTO createNotification(NotificationDTO dto) {
        log.info("Execute method createNotification()");
        Optional<User> optionalUser = userRepository.findById(dto.getUserId());

        if (optionalUser.isEmpty()) {
            throw new CustomException(404, "User not found");
        }

        Notification notification = new Notification();
        notification.setMessage(dto.getMessage());
        notification.setUser(optionalUser.get());

        Notification save = notificationRepository.save(notification);

        return new NotificationDTO(save.getNotificationId(),save.getMessage(), save.getUser().getUserId());
    }

    @Override
    public List<NotificationDTO> getByUserId(Long userId) {
        if (!recordAccess.hasRole("ADMIN") && !recordAccess.user().getUserId().equals(userId)) {
            throw new CustomException(403, "Notification access denied");
        }


        log.info("Execute method getByUserId()");
        Optional<User> optionalUser = userRepository.findByUserId(userId);

        if (optionalUser.isEmpty()) {
            throw new CustomException(404, "User not found");
        }

        List<Notification> notifications = notificationRepository.findTop50ByUser_UserIdOrderByNotificationIdDesc(optionalUser.get().getUserId());




        List<NotificationDTO> notificationDTOS = new ArrayList<>();

        for (Notification notification : notifications) {
            NotificationDTO notificationDTO = new NotificationDTO(notification.getNotificationId(), notification.getMessage(), notification.getUser().getUserId());
            notificationDTOS.add(notificationDTO);
        }

        return notificationDTOS;
    }
}
