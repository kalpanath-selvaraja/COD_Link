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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@org.springframework.transaction.annotation.Transactional
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
        log.info("Execute method createCourierAccount()");
        Optional<User> optionalUser = userRepository.findByUsername(courierDTO.getUserName());

        if(optionalUser.isPresent()){
            log.error("Username already exists: {}", courierDTO.getUserName());
            throw new CustomException(409,"Username already exists");
        }

        Optional<Branch> optionalBranch = branchRepository.findById(courierDTO.getBranchId());

        if(optionalBranch.isEmpty()){
            log.error("Branch not found with ID: {}", courierDTO.getBranchId());
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
    public CourierDTO getMyProfile(String username) {
        log.info("Execute method getMyProfile()");
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            log.error("User not found with username: {}", username);
            throw new CustomException(404, "User not found");
        }
        Optional<Courier> optionalCourier = courierRepository.findByUser_UserId(optionalUser.get().getUserId());
        if (optionalCourier.isEmpty()) {
            log.error("No courier profile linked to user ID: {}", optionalUser.get().getUserId());
            throw new CustomException(404, "No courier profile linked to this account");
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
    public CourierDTO getById(Long id) {
        log.info("Execute method getById()");
        Optional<Courier> optionalCourier = courierRepository.findById(id);

        if(optionalCourier.isEmpty()){
            log.error("Courier not found with ID: {}", id);
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
        log.info("Execute method getByBranch()");
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);

        if(optionalBranch.isEmpty()){
            log.error("Branch not found with ID: {}", branchId);
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
        log.info("Execute method updateCourierAccount()");

        Optional<Courier> optionalCourier = courierRepository.findById(courierId);
        if(optionalCourier.isEmpty()){
            log.error("Courier not found with ID: {}", courierId);
            throw new CustomException(404,"Courier not found");
        }

        Courier courier = optionalCourier.get();

        if (courierDTO.getLicenseNumber() != null) {
            courier.setLicenseNumber(courierDTO.getLicenseNumber());
        }
        if (courierDTO.getBranchId() != null) {
            Optional<Branch> optionalBranch = branchRepository.findById(courierDTO.getBranchId());
            if (optionalBranch.isEmpty()) {
                log.error("Branch not found with ID: {}", courierDTO.getBranchId());
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
        log.info("Execute method delete()");
        Optional<Courier> optionalCourier = courierRepository.findById(courierId);
        if (optionalCourier.isEmpty()) {
            log.error("Courier not found with ID: {}", courierId);
            throw new CustomException(404, "Courier not found");
        }

        try {
            courierRepository.delete(optionalCourier.get());
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while deleting courier: {}", e.getMessage());
            throw new CustomException(409, "Cannot delete — this courier is still referenced by delivery attempts, COD collections, or vehicles. Remove or reassign those first.");
        }
    }

    @Override
    public List<CourierDTO> getAll() {
        log.info("Execute method getAll()");
        List<CourierDTO> list = new ArrayList<>();
        for (Courier courier : courierRepository.findAll()) {
            CourierDTO dto = new CourierDTO();
            dto.setCourierId(courier.getCourierId());
            dto.setLicenseNumber(courier.getLicenseNumber());
            dto.setUserId(courier.getUser().getUserId());
            dto.setBranchId(courier.getBranch().getBranchId());
            dto.setPassword(null);
            dto.setUserName(null);
            list.add(dto);
        }
        return list;
    }
}
