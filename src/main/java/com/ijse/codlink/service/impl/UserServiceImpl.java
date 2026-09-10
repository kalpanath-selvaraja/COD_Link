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

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new CustomException(403, "Account is deactivated");
        }

        return new UserDTO(user.getUserId(), user.getUsername(), user.getRole(), user.getActive(), user.getIsReceptionist());
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
        user.setRole(UserRole.USER);
        userRepository.save(user);


    }

    @Override
    public UserDTO updateStatus(Long userId, boolean active) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new CustomException(404, "User not found");
        }
        User user = optionalUser.get();
        user.setActive(active);
        User saved = userRepository.save(user);
        return new UserDTO(saved.getUserId(), saved.getUsername(), saved.getRole(), saved.getActive(), saved.getIsReceptionist());
    }

    @Override
    public UserDTO createAdminAccount(RegisterRequestDTO request) {
        Optional<User> existing = userRepository.findByUsername(request.getUsername());
        if (existing.isPresent()) {
            throw new CustomException(409, "Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if(request.getIsReceptionist() == true){
            user.setRole(UserRole.RECEPTIONIST);
            user.setIsReceptionist(Boolean.TRUE);
        }else {
            user.setRole(UserRole.ADMIN);
            user.setIsReceptionist(Boolean.FALSE);

        }

        user.setActive(true);
       user.setIsReceptionist(Boolean.TRUE.equals(request.getIsReceptionist()));

        User saved = userRepository.save(user);
        return new UserDTO(saved.getUserId(), saved.getUsername(), saved.getRole(), saved.getActive(), saved.getIsReceptionist());
    }
}