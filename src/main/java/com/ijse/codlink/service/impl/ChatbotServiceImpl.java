package com.ijse.codlink.service.impl;

import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.*;
import com.ijse.codlink.service.ChatbotService;
import com.ijse.codlink.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {
    private final GeminiService geminiService;
    private final ParcelRepository parcelRepository;
    private final CustomerRepository customerRepository;
    private final CodCollectionRepository codCollectionRepository;
    private final PaymentRepository paymentRepository;
    private static final ZoneId REPORT_ZONE = ZoneId.of("Asia/Colombo");

    public static LocalDateTime weekStart(Instant instant) {
        return instant.atZone(REPORT_ZONE).toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
    }

    @Override
    public String handleQuery(String role, String message) {
        if (!"ROLE_ADMIN".equals(role)) throw new CustomException(403, "Admin access required");
        if (message == null || message.isBlank() || message.length() > 2000) {
            throw new CustomException(400, "Enter a question of up to 2000 characters");
        }
        Instant now = Instant.now();
        LocalDateTime start = weekStart(now);
        var parcels = parcelRepository.findAll();
        StringBuilder prompt = new StringBuilder("Answer only from these CODLink totals. Do not invent missing data. Currency: LKR.\n");
        for (ParcelStatus status : ParcelStatus.values()) {
            prompt.append(status).append(": ").append(parcels.stream().filter(p -> p.getStatus() == status).count()).append('\n');
        }
        prompt.append("Customers: ").append(customerRepository.count()).append('\n');
        prompt.append("Unreconciled collections: ").append(codCollectionRepository.findAll().stream()
                .filter(c -> c.getReconciliationBatch() == null).count()).append('\n');
        prompt.append("Week starts Monday, Asia/Colombo. This week to date from ").append(start).append(": ")
                .append(paymentRepository.sumReceiptsBetween(start, LocalDateTime.ofInstant(now, REPORT_ZONE))).append('\n');
        prompt.append("Previous complete week from ").append(start.minusWeeks(1)).append(" to ").append(start).append(": ")
                .append(paymentRepository.sumReceiptsBetween(start.minusWeeks(1), start)).append('\n');
        prompt.append("Weekly totals include recorded service-charge receipts only, not COD goods or unreviewed historical payments.\n");
        prompt.append("Question: ").append(message).append("\nAnswer concisely.");
        return geminiService.generateReply(prompt.toString());
    }
}
