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

    private final BranchTransferRepository branchTransferRepository;
    private final ParcelRepository parcelRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public BranchTransferDTO create(BranchTransferDTO dto) {
        if (dto.getParcelId() == null) {
            throw new CustomException(400, "parcelId is required");
        }
        if (dto.getFromBranchId() == null) {
            throw new CustomException(400, "fromBranchId is required");
        }
        if (dto.getToBranchId() == null) {
            throw new CustomException(400, "toBranchId is required");
        }

        Optional<Parcel> optionalParcel = parcelRepository.findById(dto.getParcelId());
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }
        Optional<Branch> optionalFrom = branchRepository.findById(dto.getFromBranchId());
        if (optionalFrom.isEmpty()) {
            throw new CustomException(404, "Origin branch not found");
        }
        Optional<Branch> optionalTo = branchRepository.findById(dto.getToBranchId());
        if (optionalTo.isEmpty()) {
            throw new CustomException(404, "Destination branch not found");
        }
        if (dto.getFromBranchId().equals(dto.getToBranchId())) {
            throw new CustomException(400, "Origin and destination branch must be different");
        }

        Parcel parcel = optionalParcel.get();
        Branch fromBranch = optionalFrom.get();
        Branch toBranch = optionalTo.get();

        if (parcel.getStatus() == ParcelStatus.DELIVERED
                || parcel.getStatus() == ParcelStatus.RETURNED) {
            throw new CustomException(409,
                    "Cannot transfer a parcel — parcel status is " + parcel.getStatus());
        }

        Branch actualCurrentBranch = parcel.getCurrentBranch() != null
                ? parcel.getCurrentBranch()
                : parcel.getOriginBranch();

        if (!actualCurrentBranch.getBranchId().equals(fromBranch.getBranchId())) {
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
        Optional<Parcel> optionalParcel = parcelRepository.findById(parcelId);
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }
        List<BranchTransferDTO> list = new ArrayList<>();
        for (BranchTransfer t : branchTransferRepository.findByParcel_ParcelId(parcelId)) {
            list.add(toDto(t));
        }
        return list;
    }

    private BranchTransferDTO toDto(BranchTransfer t) {
        return new BranchTransferDTO(t.getTransferId(), t.getParcel().getParcelId(),
                t.getFromBranch().getBranchId(), t.getToBranch().getBranchId(), t.getDispatchedAt());
    }
}