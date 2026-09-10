package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.ChatbotRequestDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse chat(@Valid @RequestBody ChatbotRequestDTO dto) {
        return new CommonResponse(0, chatbotService.handleQuery(dto.getMessage()), "Chatbot response");
    }
}