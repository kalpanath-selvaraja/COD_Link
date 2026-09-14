package com.ijse.codlink.service.impl;

import com.ijse.codlink.entity.Customer;
import com.ijse.codlink.entity.Parcel;
import com.ijse.codlink.entity.User;
import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.enumeration.UserRole;
import com.ijse.codlink.enumeration.UserStatus;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.*;
import com.ijse.codlink.service.ChatbotService;
import com.ijse.codlink.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// ChatbotServiceImpl.java
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ParcelRepository parcelRepository;
    private final CustomerRepository customerRepository;
    private final CodCollectionRepository codCollectionRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public String handleQuery(String role, String message) {


//        boolean isReceptionist = Boolean.TRUE.equals(optionalUser.get().getIsReceptionist());
        System.out.println(role); // ROLE_ADMIN
        String systemPrompt = "";

        if("ROLE_ADMIN".equals(role)){
            systemPrompt = buildAdminPrompt();
        } else if (role.equals(UserRole.RECEPTIONIST)) {
            systemPrompt = buildReceptionistPrompt();
        }

//        String systemPrompt = isReceptionist ? buildReceptionistPrompt() : buildAdminPrompt();

        String fullPrompt = systemPrompt +
                "\n\nQuestion: " + message +
                "\n\nAnswer directly and concisely. If the question asks for data not listed above, " +
                "say you don't have access to it rather than guessing.";

        return geminiService.generateReply(fullPrompt);
    }

    private String buildAdminPrompt() {
        List<Parcel> parcels = parcelRepository.findAll();
        long inTransit = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_TRANSIT).count();
        long delivered = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.DELIVERED).count();
        long failed = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.FAILED).count();
        long booked = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.BOOKED).count();
        long linked = customerRepository.countByUserIsNotNull();
        List<Customer> customers = customerRepository.findAll();
        long walkIn = customerRepository.countByUserIsNull();





        BigDecimal revenue = invoiceRepository.sumAmountWithPayment();
        long unreconciled = codCollectionRepository.findAll().stream()
                .filter(c -> c.getReconciliationBatch() == null)
                .count();

        return """
            You are an operations assistant for a CODLink administrator with full system-wide visibility.

            PARCELS: %d total — %d in transit, %d delivered, %d failed.
            CUSTOMERS: %d total — %d registered, %d walk-in.
            REVENUE: $%s collected against paid invoices.
            UNRECONCILED COD COLLECTIONS: %d.
            And any other provided information

            You may discuss revenue, reconciliation status, customers , users and branch-wide parcel volume and any else.
            Flag anything needing urgent attention (e.g. a high failed-delivery count).
            """.formatted(parcels.size(), inTransit, delivered, failed,
                linked + walkIn, linked, walkIn, revenue, unreconciled , customers,booked
        );
    }

    private String buildReceptionistPrompt() {
        List<Parcel> parcels = parcelRepository.findAll();
        long booked = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.BOOKED).count();
        long inTransit = parcels.stream().filter(p -> p.getStatus() == ParcelStatus.IN_TRANSIT).count();
        List<Customer> customers = customerRepository.findAll();
        return """
            You are a front-desk assistant for a CODLink receptionist. You help with parcel status
            lookups, walk-in customer questions, and booking guidance.

            PARCELS AWAITING PICKUP: %d
            PARCELS IN TRANSIT: %d

            IMPORTANT RULES:
            - Do NOT provide total revenue, invoice amounts, or payment records
            - Do NOT provide reconciliation batch totals or COD collection sums
            - Do NOT provide audit log or courier performance data
            - If asked for financial or admin-level reporting, say that's outside receptionist
              access and to check with an administrator
            """.formatted(booked, inTransit,customers,parcels);
    }


}