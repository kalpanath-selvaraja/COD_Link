package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.ChatbotRequestDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    // ChatbotController.java
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse chat(@jakarta.validation.Valid @RequestBody ChatbotRequestDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return new CommonResponse(0, chatbotService.handleQuery(role, dto.getMessage()), "Chatbot response");
    }
}