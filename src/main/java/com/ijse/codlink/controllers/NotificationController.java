package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.NotificationDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/notification")
public class NotificationController {

    private final NotificationService notificationService;


    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public CommonResponse createNotification(@RequestBody NotificationDTO notificationDTO) {
        NotificationDTO notification = notificationService.createNotification(notificationDTO);

        return new CommonResponse(0,notification,"Notification created successfully");
    }

    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getUser(@PathVariable Long userId) {
        List<NotificationDTO> byUserId = notificationService.getByUserId(userId);

        return new CommonResponse(0,byUserId,"Notification retrieved successfully");
    }
}
