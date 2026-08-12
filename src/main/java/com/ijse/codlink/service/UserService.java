package com.ijse.codlink.service;

import com.ijse.codlink.dto.AuthDTO;
import com.ijse.codlink.dto.RegisterRequestDTO;
import com.ijse.codlink.dto.UserDTO;

public interface UserService {

    UserDTO getUserDetails(AuthDTO authDTO);

    void saveUser(RegisterRequestDTO registerRequestDTO);
}
