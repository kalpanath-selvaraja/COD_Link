package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.AddressDTO;
import com.ijse.codlink.dto.BranchDTO;
import com.ijse.codlink.entity.Address;
import com.ijse.codlink.entity.Branch;
import com.ijse.codlink.entity.ServiceZone;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.BranchRepository;
import com.ijse.codlink.repository.ServiceZoneRepository;
import com.ijse.codlink.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@org.springframework.transaction.annotation.Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
@Slf4j
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final ServiceZoneRepository serviceZoneRepository;

    @Override
    public BranchDTO createBranch(BranchDTO branchDTO) {
        log.info("Execute method createBranch()");
        if(branchDTO.getBranchName() == null || branchDTO.getBranchName().isBlank()){
            log.error("Branch name is either null or blank.");
            throw new CustomException(400,"branch name is required");
        }

        Branch branch = new Branch();
        branch.setBranchName(branchDTO.getBranchName());
        branch.setPhone(branchDTO.getPhone());


        AddressDTO branchDTOAddress = branchDTO.getAddress();

        if (branchDTOAddress == null) {
            log.error("Branch address is null.");
            throw new CustomException(400, "Branch address is required");
        }
        if (branchDTOAddress.getZoneId() == null) {
            log.error("Zone ID is null.");
            throw new CustomException(400, "Branch address service zone ID is required");
        }

        Optional<ServiceZone> zone = serviceZoneRepository.findById(branchDTOAddress.getZoneId());
        if (zone.isEmpty()) {
            log.error("Could not find zone by zone ID.");
            throw new CustomException(404, "Service zone not found");
        }

        Address address = new Address();
        address.setStreet(branchDTOAddress.getStreet());
        address.setCity(branchDTOAddress.getCity());
        address.setPostalCode(branchDTOAddress.getPostalCode());
        address.setServiceZone(zone.get());

        branch.setAddress(address);
        log.info("Branch Created");
        return toDto(branchRepository.save(branch));
    }

    @Override
    public BranchDTO getById(Long branchId) {
        log.info("Execute method getById()");
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            log.error("Could not find branch by branch ID.");
            throw new CustomException(404, "Branch not found");
        }
        log.info("Branch fetched by ID");

        return toDto(optionalBranch.get());

    }

    @Override
    public List<BranchDTO> getAll() {
        log.info("Execute method getAll()");
        List<BranchDTO> list = new ArrayList<>();
        for (Branch branch : branchRepository.findAll()) {
            list.add(toDto(branch));
        }
        log.info("Branch fetched");

        return list;
    }

    @Override
    public BranchDTO updateBranch(Long branchId, BranchDTO branchDTO) {

        log.info("Execute method updateBranch()");

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            log.error("Could not find branch by branch ID.");
            throw new CustomException(404, "Branch not found");
        }
        Branch branch = optionalBranch.get();
        if (branchDTO.getBranchName() != null) branch.setBranchName(branchDTO.getBranchName());
        if (branchDTO.getPhone() != null) branch.setPhone(branchDTO.getPhone());

        log.info("Branch updated. ");

        return toDto(branchRepository.save(branch));
    }

    @Override
    public void deleteBranch(Long branchId) {
        log.info("Execute method deleteBranch()");

        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            log.error("Could not find branch by branch ID.");
            throw new CustomException(404, "Branch not found");
        }
        try {

            branchRepository.delete(optionalBranch.get());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.info("Data integrity exception: trying to delete while the other tables are referencing it.");
            throw new CustomException(409, "Cannot delete - this branch is still referenced by couriers, parcels, or other records.");
        }

    }

    private BranchDTO toDto(Branch branch) {
        log.info("Execute method toDto()");
        AddressDTO addressDTO = null;
        if (branch.getAddress() != null) {
            Address a = branch.getAddress();
            addressDTO = new AddressDTO(a.getAddressId(), a.getStreet(), a.getCity(), a.getPostalCode(),
                    a.getServiceZone() != null ? a.getServiceZone().getZoneId() : null);
        }
        return new BranchDTO(branch.getBranchId(), branch.getBranchName(), branch.getPhone(), addressDTO);
    }
}
