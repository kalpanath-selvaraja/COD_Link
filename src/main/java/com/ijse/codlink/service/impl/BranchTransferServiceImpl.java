package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.BranchTransferDTO;
import com.ijse.codlink.entity.Branch;
import com.ijse.codlink.entity.BranchTransfer;
import com.ijse.codlink.entity.Parcel;
import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.BranchRepository;
import com.ijse.codlink.repository.BranchTransferRepository;
import com.ijse.codlink.repository.ParcelRepository;
import com.ijse.codlink.service.BranchTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BranchTransferServiceImpl implements BranchTransferService {
    private final com.ijse.codlink.security.RecordAccess recordAccess;

    private final BranchTransferRepository branchTransferRepository;
    private final ParcelRepository parcelRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public BranchTransferDTO create(BranchTransferDTO dto) {
        log.info("Execute method create()");
        if (dto.getParcelId() == null) {
            log.error("Parcel ID is required");
            throw new CustomException(400, "parcelId is required");
        }
        if (dto.getFromBranchId() == null) {
            log.error("From branch ID is required");
            throw new CustomException(400, "fromBranchId is required");
        }
        if (dto.getToBranchId() == null) {
            log.error("To branch ID is required");
            throw new CustomException(400, "toBranchId is required");
        }

        Optional<Parcel> optionalParcel = parcelRepository.findById(dto.getParcelId());
        if (optionalParcel.isEmpty()) {
            log.error("Parcel not found with ID: {}", dto.getParcelId());
            throw new CustomException(404, "Parcel not found");
        }
        Optional<Branch> optionalFrom = branchRepository.findById(dto.getFromBranchId());
        if (optionalFrom.isEmpty()) {
            log.error("Origin branch not found with ID: {}", dto.getFromBranchId());
            throw new CustomException(404, "Origin branch not found");
        }
        Optional<Branch> optionalTo = branchRepository.findById(dto.getToBranchId());
        if (optionalTo.isEmpty()) {
            log.error("Destination branch not found with ID: {}", dto.getToBranchId());
            throw new CustomException(404, "Destination branch not found");
        }
        if (dto.getFromBranchId().equals(dto.getToBranchId())) {
            log.error("Origin and destination branch must be different");
            throw new CustomException(400, "Origin and destination branch must be different");
        }

        Parcel parcel = optionalParcel.get();
        Branch fromBranch = optionalFrom.get();
        Branch toBranch = optionalTo.get();

        if (parcel.getStatus() == ParcelStatus.DELIVERED
                || parcel.getStatus() == ParcelStatus.RETURNED) {
            log.error("Cannot transfer a parcel — parcel status is {}", parcel.getStatus());
            throw new CustomException(409,
                    "Cannot transfer a parcel — parcel status is " + parcel.getStatus());
        }

        Branch actualCurrentBranch = parcel.getCurrentBranch() != null
                ? parcel.getCurrentBranch()
                : parcel.getOriginBranch();

        if (!actualCurrentBranch.getBranchId().equals(fromBranch.getBranchId())) {
            log.error("Origin branch does not match parcel's actual current branch (#{})", actualCurrentBranch.getBranchId());
            throw new CustomException(409,
                    "Origin branch does not match parcel's actual current branch (#"
                            + actualCurrentBranch.getBranchId() + ")");
        }

        BranchTransfer transfer = new BranchTransfer();
        transfer.setParcel(parcel);
        transfer.setFromBranch(fromBranch);
        transfer.setToBranch(toBranch);
        transfer.setDispatchedAt(LocalDateTime.now());

        parcel.setCurrentBranch(toBranch);
        parcelRepository.save(parcel);

        return toDto(branchTransferRepository.save(transfer));
    }

    @Override
    public List<BranchTransferDTO> getByParcelId(Long parcelId) {
        recordAccess.parcel(parcelId);
        log.info("Execute method getByParcelId()");
        Optional<Parcel> optionalParcel = parcelRepository.findById(parcelId);
        if (optionalParcel.isEmpty()) {
            log.error("Parcel not found with ID: {}", parcelId);
            throw new CustomException(404, "Parcel not found");
        }
        List<BranchTransferDTO> list = new ArrayList<>();
        for (BranchTransfer t : branchTransferRepository.findByParcel_ParcelId(parcelId)) {
            list.add(toDto(t));
        }
        return list;
    }

    @Override
    public List<BranchTransferDTO> getAll() {
        log.info("Execute method getAll()");
        List<BranchTransferDTO> list = new ArrayList<>();

        for(BranchTransfer b :branchTransferRepository.findAll()){
            if (recordAccess.canRead(b.getParcel())) list.add(toDto(b));
        }

        return list;

    }

    private BranchTransferDTO toDto(BranchTransfer t) {
        log.info("Execute method toDto()");
        return new BranchTransferDTO(t.getTransferId(), t.getParcel().getParcelId(),
                t.getFromBranch().getBranchId(), t.getToBranch().getBranchId(), t.getDispatchedAt());
    }
}