package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.AuditLogDTO;
import com.ijse.codlink.entity.AuditLog;
import com.ijse.codlink.entity.User;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.AuditLogRepository;
import com.ijse.codlink.repository.UserRepository;
import com.ijse.codlink.service.AuditLogService;
import com.ijse.codlink.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;


    @Override
    public AuditLogDTO createAuditLog(AuditLogDTO auditLogDTO) {
        Optional<User> optionalUser = userRepository.findByUserId(auditLogDTO.getUserId());

        if(optionalUser.isEmpty()){
            throw  new CustomException(0, "User not Found");
        }



        AuditLog log = new AuditLog();
        log.setAction(auditLogDTO.getAction());
        log.setLogTime(LocalDateTime.now());
        log.setUser(optionalUser.get());
        AuditLog saved = auditLogRepository.save(log);

        return new AuditLogDTO(saved.getLogId(), saved.getUser().getUserId(), saved.getAction(), saved.getLogTime());

    }

    @Override
    public List<AuditLogDTO> getByUserId(Long userId) {

        List<AuditLog> logList = auditLogRepository.findByUser_UserId(userId);



        List<AuditLogDTO> auditLogDTOList = new ArrayList<>();

        for(AuditLog auditLog : logList){
            AuditLogDTO auditLogDTO = new AuditLogDTO();

            auditLogDTO.setLogId(auditLog.getLogId());
            auditLogDTO.setUserId(auditLog.getUser().getUserId());
            auditLogDTO.setAction(auditLog.getAction());
            auditLogDTO.setLogTime(LocalDateTime.now());
            auditLogDTOList.add(auditLogDTO);
        }

        return auditLogDTOList;
    }
}
