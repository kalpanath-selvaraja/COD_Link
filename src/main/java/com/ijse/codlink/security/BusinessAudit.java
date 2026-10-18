package com.ijse.codlink.security;

import com.ijse.codlink.entity.AuditLog;
import com.ijse.codlink.repository.AuditLogRepository;
import com.ijse.codlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class BusinessAudit {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @AfterReturning("execution(public * com.ijse.codlink.service.impl.*.*(..)) && !within(com.ijse.codlink.service.impl.AuditLogServiceImpl)")
    public void record(JoinPoint call) {
        String method = call.getSignature().getName();
        if (!method.matches("(create|update|delete|assign|claim|reassign|book|finalize|cancel|link|resolve).*")) return;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return;
        var actor = userRepository.findByUsername(authentication.getName());
        if (actor.isEmpty()) return;
        String action = call.getSignature().getDeclaringType().getSimpleName().replace("ServiceImpl", "") + "." + method;
        if (call.getArgs().length > 0 && call.getArgs()[0] instanceof Long id) action += " #" + id;
        AuditLog audit = new AuditLog();
        audit.setUser(actor.get());
        audit.setAction(action);
        audit.setLogTime(LocalDateTime.now(ZoneId.of("Asia/Colombo")));
        auditLogRepository.save(audit);
    }
}
