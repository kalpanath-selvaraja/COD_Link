package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.AuthDTO;
import com.ijse.codlink.dto.RegisterRequestDTO;
import com.ijse.codlink.dto.UserDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.responce.UserResponseDTO;
import com.ijse.codlink.security.JwtUtil;
import com.ijse.codlink.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> login(@Valid @RequestBody AuthDTO authDTO) {
        UserDTO userDetails = userService.getUserDetails(authDTO);
        String token = jwtUtil.generateToken(userDetails);

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setToken(token);
        userResponseDTO.setUserId(userDetails.getUserId());
        userResponseDTO.setRole(userDetails.getUserRole());
        userResponseDTO.setIsReceptionist(userDetails.getIsReceptionist());
        userResponseDTO.setUsername(userDetails.getUsername());

        CommonResponse response = new CommonResponse(0, userResponseDTO, "JWT token");
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/testing")
    public ResponseEntity<String> testSecurity() {
        return ResponseEntity.ok("API Security Successful");
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse> register(@Valid @RequestBody RegisterRequestDTO request) {
        userService.saveUser(request);
        CommonResponse response = new CommonResponse(0, "User saved successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}