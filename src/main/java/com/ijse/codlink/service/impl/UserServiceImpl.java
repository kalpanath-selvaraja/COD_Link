package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.AuthDTO;
import com.ijse.codlink.dto.RegisterRequestDTO;
import com.ijse.codlink.dto.UserDTO;
import com.ijse.codlink.entity.User;
import com.ijse.codlink.enumeration.UserRole;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.UserRepository;
import com.ijse.codlink.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO getUserDetails(AuthDTO authDTO) {
        String password = authDTO.getPassword();

        Optional<User> optionalUser = userRepository.findByUsername(authDTO.getUsername());

        if(optionalUser.isEmpty()){
            throw new CustomException(401, "Invalid username or password");
        }

        User user = optionalUser.get();
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new CustomException(401, "Invalid username or password");
        }


        return new UserDTO(user.getUserId(), user.getUsername(), user.getRole());

    }

    @Override
    public void saveUser(RegisterRequestDTO  request) {

            Optional<User> existing = userRepository.findByUsername(request.getUsername());
            if (existing.isPresent()) {
                throw new CustomException(409, "Username already taken");
            }

            User user = new User();

            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(UserRole.USER); // deliberate — public self-registration is always USER, never ADMIN/COURIER
            userRepository.save(user);


    }
}
