package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.NotificationDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final com.ijse.codlink.security.RecordAccess recordAccess;

    @GetMapping("/me")
    public ResponseEntity<CommonResponse> mine() {
        return ResponseEntity.ok(new CommonResponse(0, notificationService.getByUserId(recordAccess.user().getUserId()), "Notifications fetched"));
    }


    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> createNotification(@jakarta.validation.Valid @RequestBody NotificationDTO notificationDTO) {
        NotificationDTO notification = notificationService.createNotification(notificationDTO);
        CommonResponse response = new CommonResponse(0, notification, "Notification created successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getUser(@PathVariable Long userId) {
        List<NotificationDTO> byUserId = notificationService.getByUserId(userId);
        CommonResponse response = new CommonResponse(0, byUserId, "Notification retrieved successfully");
        return ResponseEntity.ok(response);
    }
}