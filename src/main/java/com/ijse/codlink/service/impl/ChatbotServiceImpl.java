package com.ijse.codlink.service.impl;

import com.ijse.codlink.repository.CustomerRepository;
import com.ijse.codlink.repository.InvoiceRepository;
import com.ijse.codlink.service.ChatbotService;
import com.ijse.codlink.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final GeminiService geminiService;

    private static final String OUT_OF_SCOPE =
            "I can only answer questions about total sales, weekly sales, or total customers.";

    @Override
    public String handleQuery(String message) {
        if (message == null || message.isBlank()) {
            return OUT_OF_SCOPE;
        }
        String lower = message.toLowerCase();

        if (lower.contains("week")) {
            BigDecimal weekly = invoiceRepository.sumAmountWithPaymentSince(LocalDateTime.now().minusDays(7));
            return geminiService.generateReply(
                    "The total sales collected in the last 7 days is $" + weekly +
                            ". Phrase this as a single friendly sentence for an admin dashboard. No extra commentary.");
        }
        if (lower.contains("sale") || lower.contains("revenue")) {
            BigDecimal total = invoiceRepository.sumAmountWithPayment();
            return geminiService.generateReply(
                    "The total sales collected so far (paid invoices only) is $" + total +
                            ". Phrase this as a single friendly sentence for an admin dashboard. No extra commentary.");
        }
        if (lower.contains("customer")) {
            long linked = customerRepository.countByUserIsNotNull();
            long walkIn = customerRepository.countByUserIsNull();
            return geminiService.generateReply(String.format(
                    "There are %d total customers: %d with linked accounts and %d walk-in customers. " +
                            "Phrase this as a single friendly sentence for an admin dashboard. No extra commentary.",
                    linked + walkIn, linked, walkIn));
        }
        return OUT_OF_SCOPE;
    }
}