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
@Slf4j
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    
    
    private final BranchRepository branchRepository;
    private final ServiceZoneRepository serviceZoneRepository;
    
    @Override
    public BranchDTO createBranch(BranchDTO branchDTO) {
        
        if(branchDTO.getBranchName() == null || branchDTO.getBranchName().isBlank()){
            throw new CustomException(400,"branch name is required");
        }

        Branch branch = new Branch();
        branch.setBranchName(branchDTO.getBranchName());
        branch.setPhone(branchDTO.getPhone());


        AddressDTO branchDTOAddress = branchDTO.getAddress();

        if (branchDTOAddress == null) {
            throw new CustomException(400, "Branch address is required");
        }
        Address address = new Address();

        address.setStreet(branchDTOAddress.getStreet());
        address.setCity(branchDTOAddress.getCity());
        address.setPostalCode(branchDTOAddress.getPostalCode());
        if (branchDTOAddress.getZoneId() != null) {
            Optional<ServiceZone> zone = serviceZoneRepository.findById(branchDTOAddress.getZoneId());
            if (zone.isEmpty()) {
                throw new CustomException(404, "Service zone not found");
            }
            address.setServiceZone(zone.get());
        }

        branch.setAddress(address);

        return toDto(branchRepository.save(branch));
    }

    @Override
    public BranchDTO getById(Long branchId) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            throw new CustomException(404, "Branch not found");
        }
        return toDto(optionalBranch.get());

    }

    @Override
    public List<BranchDTO> getAll() {
        List<BranchDTO> list = new ArrayList<>();
        for (Branch branch : branchRepository.findAll()) {
            list.add(toDto(branch));
        }
        return list;
    }

    @Override
    public BranchDTO updateBranch(Long branchId, BranchDTO branchDTO) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            throw new CustomException(404, "Branch not found");
        }
        Branch branch = optionalBranch.get();
        if (branchDTO.getBranchName() != null) branch.setBranchName(branchDTO.getBranchName());
        if (branchDTO.getPhone() != null) branch.setPhone(branchDTO.getPhone());
        // Address is intentionally not replaced on update a branch keeps its
        // original address row If it truly relocates, delete and recreate.  (new way)
        return toDto(branchRepository.save(branch));
    }

    @Override
    public void deleteBranch(Long branchId) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            throw new CustomException(404, "Branch not found");
        }
        branchRepository.delete(optionalBranch.get());
    }

    private BranchDTO toDto(Branch branch) {
        AddressDTO addressDTO = null;
        if (branch.getAddress() != null) {
            Address a = branch.getAddress();
            addressDTO = new AddressDTO(a.getAddressId(), a.getStreet(), a.getCity(), a.getPostalCode(),
                    a.getServiceZone() != null ? a.getServiceZone().getZoneId() : null);
        }
        return new BranchDTO(branch.getBranchId(), branch.getBranchName(), branch.getPhone(), addressDTO);
    }
}
