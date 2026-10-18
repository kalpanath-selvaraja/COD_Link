package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.UserStatusUpdateDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> updateStatus(@PathVariable Long id, @jakarta.validation.Valid @RequestBody UserStatusUpdateDTO dto) {
        CommonResponse response = new CommonResponse(0, userService.updateStatus(id, dto.isActive()), "User status updated");
        return ResponseEntity.ok(response);
    }
}