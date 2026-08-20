package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.CourierDTO;
import com.ijse.codlink.entity.Branch;
import com.ijse.codlink.entity.Courier;
import com.ijse.codlink.entity.User;
import com.ijse.codlink.enumeration.UserRole;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.BranchRepository;
import com.ijse.codlink.repository.CourierRepository;
import com.ijse.codlink.repository.UserRepository;
import com.ijse.codlink.service.CourierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourierServiceImpl implements CourierService {

    private final CourierRepository courierRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public CourierDTO createCourierAccount(CourierDTO courierDTO) {
        Optional<User> optionalUser = userRepository.findByUsername(courierDTO.getUserName());

        if(optionalUser.isPresent()){
            throw new CustomException(409,"Username already exists");
        }

        Optional<Branch> optionalBranch = branchRepository.findById(courierDTO.getBranchId());

        if(optionalBranch.isEmpty()){
            throw new CustomException(404,"Branch not found");
        }

        User user = new User();
        user.setUsername(courierDTO.getUserName());
        user.setPassword(passwordEncoder.encode(courierDTO.getPassword()));
        user.setRole(UserRole.COURIER);
        User savedUser = userRepository.save(user);

        Courier courier = new Courier();
        courier.setLicenseNumber(courierDTO.getLicenseNumber());
        courier.setUser(savedUser);
        courier.setBranch(optionalBranch.get());
        Courier saved = courierRepository.save(courier);

        CourierDTO dto = new CourierDTO();
        dto.setCourierId(saved.getCourierId());
        dto.setLicenseNumber(saved.getLicenseNumber());
        dto.setUserId(saved.getUser().getUserId());
        dto.setBranchId(saved.getBranch().getBranchId());
        dto.setPassword(null);
        dto.setUserName(null);


        return dto;
    }



    @Override
    public CourierDTO getById(Long id) {
        Optional<Courier> optionalCourier = courierRepository.findById(id);

        if(optionalCourier.isEmpty()){
            throw new CustomException(404,"Courier not found");
        }
        Courier courier = optionalCourier.get();

        CourierDTO dto = new CourierDTO();

        dto.setCourierId(courier.getCourierId());
        dto.setLicenseNumber(courier.getLicenseNumber());
        dto.setUserId(courier.getUser().getUserId());
        dto.setBranchId(courier.getBranch().getBranchId());
        dto.setPassword(null);
        dto.setUserName(null);

        return dto;
    }

    @Override
    public List<CourierDTO> getByBranch(Long branchId) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);

        if(optionalBranch.isEmpty()){
            throw new CustomException(404,"Branch not found");
        }

        List<Courier> courierList = courierRepository.findByBranch_BranchId(optionalBranch.get().getBranchId());
        List<CourierDTO> courierDTOList = new ArrayList<>();

        for (Courier courier : courierList) {
            CourierDTO dto = new CourierDTO();

            dto.setCourierId(courier.getCourierId());
            dto.setLicenseNumber(courier.getLicenseNumber());
            dto.setUserId(courier.getUser().getUserId());
            dto.setBranchId(courier.getBranch().getBranchId());
            dto.setPassword(null);
            dto.setUserName(null);

            courierDTOList.add(dto);
        }


        return courierDTOList;
    }

    @Override
    public CourierDTO updateCourierAccount(Long courierId, CourierDTO courierDTO) {

        Optional<Courier> optionalCourier = courierRepository.findById(courierId);
        if(optionalCourier.isEmpty()){
            throw new CustomException(404,"Courier not found");
        }

        Courier courier = optionalCourier.get();

        if (courierDTO.getLicenseNumber() != null) {
            courier.setLicenseNumber(courierDTO.getLicenseNumber());
        }
        if (courierDTO.getBranchId() != null) {
            Optional<Branch> optionalBranch = branchRepository.findById(courierDTO.getBranchId());
            if (optionalBranch.isEmpty()) {
                throw new CustomException(404, "Branch not found");
            }
            courier.setBranch(optionalBranch.get());
        }
        Courier saved = courierRepository.save(courier);

        CourierDTO dto = new CourierDTO();
        dto.setCourierId(saved.getCourierId());
        dto.setLicenseNumber(saved.getLicenseNumber());
        dto.setUserId(saved.getUser().getUserId());
        dto.setBranchId(saved.getBranch().getBranchId());
        dto.setPassword(null);
        dto.setUserName(null);


        return dto;
    }

    @Override
    public void delete(Long courierId) {
        Optional<Courier> optionalCourier = courierRepository.findById(courierId);
        if (optionalCourier.isEmpty()) {
            throw new CustomException(404, "Courier not found");
        }

        courierRepository.delete(optionalCourier.get());
    }
}
