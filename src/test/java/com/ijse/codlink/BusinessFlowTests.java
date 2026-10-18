package com.ijse.codlink;

import com.ijse.codlink.dto.*;
import com.ijse.codlink.entity.*;
import com.ijse.codlink.enumeration.*;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.*;
import com.ijse.codlink.service.*;
import com.ijse.codlink.service.impl.ChatbotServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BusinessFlowTests {
    @Autowired ParcelService parcelService;
    @Autowired PaymentService paymentService;
    @Autowired InvoiceService invoiceService;
    @Autowired CodCollectionService collectionService;
    @Autowired ReconciliationBatchService reconciliationService;
    @Autowired BranchTransferService transferService;
    @Autowired NotificationService notificationService;
    @Autowired PricingRuleService pricingService;
    @Autowired UserRepository users;
    @Autowired CustomerRepository customers;
    @Autowired CourierRepository couriers;
    @Autowired BranchRepository branches;
    @Autowired ServiceZoneRepository zones;
    @Autowired PricingRuleRepository rules;
    @Autowired ParcelRepository parcels;
    @Autowired PaymentRepository payments;
    @Autowired ShipmentRepository shipments;
    @Autowired CodCollectionRepository collections;
    @Autowired AuditLogRepository audit;
    @Autowired NotificationRepository notifications;
    @Autowired MockMvc mvc;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
    User admin, owner, stranger;
    Customer customer;
    Courier first, second;
    Branch branch, otherBranch;
    ServiceZone zone;

    @BeforeEach
    void setup() {
        new org.springframework.transaction.support.TransactionTemplate(transactions).executeWithoutResult(tx -> {
        admin = account(UserRole.ADMIN);
        owner = account(UserRole.USER);
        stranger = account(UserRole.USER);
        zone = new ServiceZone(); zone.setName("Test zone"); zone = zones.save(zone);
        branch = branch("Origin"); otherBranch = branch("Other");
        first = courier(branch); second = courier(branch);
        customer = new Customer(); customer.setName("Test customer"); customer.setUser(owner);
        customer.setStatus(UserStatus.ACTIVE); customer = customers.save(customer);
        for (boolean cod : new boolean[]{true, false}) {
            PricingRule rule = new PricingRule(); rule.setServiceZone(zone); rule.setWeightBracket("0-71");
            rule.setRate(new BigDecimal("100.00")); rule.setIsCod(cod); rules.save(rule);
        }
        login(admin);
        });
    }

    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    private User account(UserRole role) {
        User user = new User(); user.setUsername(UUID.randomUUID().toString()); user.setPassword("test-password");
        user.setRole(role); user.setActive(true); return users.save(user);
    }

    private Branch branch(String name) {
        Address address = new Address(); address.setStreet("Street"); address.setCity("City");
        address.setPostalCode("00100"); address.setServiceZone(zone);
        Branch value = new Branch(); value.setBranchName(name); value.setAddress(address); return branches.save(value);
    }

    private Courier courier(Branch branch) {
        Courier courier = new Courier(); courier.setUser(account(UserRole.COURIER)); courier.setBranch(branch);
        return couriers.save(courier);
    }

    private void login(User user) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getUsername(), "",
                AuthorityUtils.createAuthorityList("ROLE_" + user.getRole().name())));
    }

    private ParcelDTO booking(boolean cod) {
        ParcelDTO dto = new ParcelDTO(); dto.setOriginBranchId(branch.getBranchId()); dto.setWeight(2.0);
        dto.setCodAmount(cod ? new BigDecimal("500.00") : BigDecimal.ZERO);
        dto.setPickupAddress(new AddressDTO(null, "Street", "City", "00100", zone.getZoneId()));
        dto.setDeliveryAddress(new AddressDTO(null, "Street", "City", "00100", zone.getZoneId()));
        return dto;
    }

    private ParcelDTO book(boolean cod) {
        return parcelService.bookCartForWalkIn(customer.getCustomerId(), List.of(booking(cod))).get(0);
    }

    private void changeStatus(Long id, ParcelStatus value) {
        ParcelDTO dto = new ParcelDTO(); dto.setStatus(value); parcelService.updateStatus(id, dto);
    }

    private InvoiceDTO invoice(ParcelDTO parcel) { return invoiceService.getByShipmentId(parcel.getShipmentId()); }

    private PaymentDTO receipt(InvoiceDTO invoice, String amount, String reference) {
        PaymentDTO dto = new PaymentDTO(); dto.setInvoiceId(invoice.getInvoiceId()); dto.setAmount(new BigDecimal(amount));
        dto.setMethod("CASH"); dto.setRequestReference(reference); return paymentService.create(dto);
    }

    private void deliver(ParcelDTO parcel) {
        login(first.getUser()); parcelService.claim(parcel.getParcelId());
        changeStatus(parcel.getParcelId(), ParcelStatus.IN_TRANSIT); changeStatus(parcel.getParcelId(), ParcelStatus.DELIVERED);
    }

    private CodCollectionDTO collect(ParcelDTO parcel) {
        CodCollectionDTO dto = new CodCollectionDTO(); dto.setParcelId(parcel.getParcelId()); dto.setAmount(new BigDecimal("600.00"));
        return collectionService.create(first.getUser().getUsername(), dto);
    }

    @Test
    void prepaidUsesPartialReceiptsAndWaitsForBothDeliveryAndBalance() {
        ParcelDTO parcel = book(false);
        InvoiceDTO invoice = invoice(parcel);
        assertEquals(0, payments.findByInvoice_InvoiceId(invoice.getInvoiceId()).size());
        receipt(invoice, "40.00", UUID.randomUUID().toString());
        assertEquals(0, invoice(parcel).getBalance().compareTo(new BigDecimal("60.00")));
        deliver(parcel);
        assertEquals(ShipmentStatus.ACTIVE, shipments.findById(parcel.getShipmentId()).orElseThrow().getStatus());
        login(admin); receipt(invoice, "60.00", UUID.randomUUID().toString());
        assertEquals(ShipmentStatus.COMPLETED, shipments.findById(parcel.getShipmentId()).orElseThrow().getStatus());
        assertThrows(CustomException.class, () -> receipt(invoice, "1.00", UUID.randomUUID().toString()));
    }

    @Test
    void returnAddsChargeAfterAnEarlierReceipt() {
        ParcelDTO parcel = book(false); InvoiceDTO invoice = invoice(parcel);
        receipt(invoice, "100.00", UUID.randomUUID().toString());
        parcelService.reassign(parcel.getParcelId(), first.getCourierId());
        changeStatus(parcel.getParcelId(), ParcelStatus.IN_TRANSIT); changeStatus(parcel.getParcelId(), ParcelStatus.FAILED);
        changeStatus(parcel.getParcelId(), ParcelStatus.RETURNED);
        assertEquals(0, invoice(parcel).getAmount().compareTo(new BigDecimal("210.00")));
        assertEquals(0, invoice(parcel).getBalance().compareTo(new BigDecimal("110.00")));
        receipt(invoice(parcel), "110.00", UUID.randomUUID().toString());
        assertEquals(ShipmentStatus.COMPLETED, shipments.findById(parcel.getShipmentId()).orElseThrow().getStatus());
    }

    @Test
    void codRequiresDeliveringCourierAndProducesOnlyOneShippingReceipt() {
        ParcelDTO parcel = book(true);
        assertThrows(CustomException.class, () -> receipt(invoice(parcel), "100.00", UUID.randomUUID().toString()));
        deliver(parcel);
        login(second.getUser()); assertThrows(CustomException.class, () -> collect(parcel));
        login(first.getUser()); collect(parcel);
        assertEquals(0, payments.sumForInvoice(invoice(parcel).getInvoiceId()).compareTo(new BigDecimal("100.00")));
        assertThrows(CustomException.class, () -> collect(parcel));
        assertEquals(1, payments.findByInvoice_InvoiceId(invoice(parcel).getInvoiceId()).size());
        assertEquals(ShipmentStatus.COMPLETED, shipments.findById(parcel.getShipmentId()).orElseThrow().getStatus());
    }

    @Test
    void concurrentClaimsHaveOneWinnerAndRetryIsIdempotent() throws Exception {
        ParcelDTO parcel = book(true);
        List<Object> results = race(first.getUser(), second.getUser(),
                () -> parcelService.claim(parcel.getParcelId()), () -> parcelService.claim(parcel.getParcelId()));
        assertEquals(1, results.stream().filter(ParcelDTO.class::isInstance).count());
        assertEquals(1, results.stream().filter(CustomException.class::isInstance).count());
        ParcelDTO won = (ParcelDTO) results.stream().filter(ParcelDTO.class::isInstance).findFirst().orElseThrow();
        login(won.getAssignedCourierId().equals(first.getCourierId()) ? first.getUser() : second.getUser());
        assertEquals(won.getAssignedCourierId(), parcelService.claim(parcel.getParcelId()).getAssignedCourierId());
    }

    @Test
    void concurrentReceiptsDoNotOverpay() throws Exception {
        ParcelDTO parcel = book(false); InvoiceDTO invoice = invoice(parcel);
        List<Object> results = race(admin, admin, () -> receipt(invoice, "60.00", UUID.randomUUID().toString()),
                () -> receipt(invoice, "60.00", UUID.randomUUID().toString()));
        assertEquals(1, results.stream().filter(PaymentDTO.class::isInstance).count());
        assertEquals(0, payments.sumForInvoice(invoice.getInvoiceId()).compareTo(new BigDecimal("60.00")));
    }

    @Test
    void concurrentRetryReturnsTheSameReceipt() throws Exception {
        InvoiceDTO invoice = invoice(book(false)); String reference = UUID.randomUUID().toString();
        List<Object> results = race(admin, admin, () -> receipt(invoice, "40.00", reference), () -> receipt(invoice, "40.00", reference));
        assertEquals(2, results.stream().filter(PaymentDTO.class::isInstance).count());
        assertEquals(((PaymentDTO) results.get(0)).getPaymentId(), ((PaymentDTO) results.get(1)).getPaymentId());
        assertEquals(1, payments.findByInvoice_InvoiceId(invoice.getInvoiceId()).size());
    }

    @Test
    void reconciliationUsesCollectionBranchAndBatchesOnlyOnce() throws Exception {
        ParcelDTO parcel = book(true); deliver(parcel); CodCollectionDTO collection = collect(parcel);
        first.setBranch(otherBranch); couriers.save(first);
        List<Object> results = race(admin, admin, () -> reconciliationService.createForBranch(branch.getBranchId()),
                () -> reconciliationService.createForBranch(branch.getBranchId()));
        assertEquals(1, results.stream().filter(ReconciliationBatchDTO.class::isInstance).count());
        CodCollection stored = collections.findById(collection.getCollectionId()).orElseThrow();
        assertEquals(branch.getBranchId(), stored.getCollectionBranch().getBranchId());
        assertEquals(branch.getBranchId(), stored.getReconciliationBatch().getBranch().getBranchId());
    }

    @Test
    void ownershipAndReceptionistPermissionsAreEnforced() throws Exception {
        ParcelDTO parcel = book(true);
        mvc.perform(get("/api/parcels/" + parcel.getParcelId()).with(user(stranger.getUsername()).roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/parcels/" + parcel.getParcelId()).with(user(owner.getUsername()).roles("USER"))).andExpect(status().isOk());
        mvc.perform(get("/api/parcels/customer/" + customer.getCustomerId()).with(user(stranger.getUsername()).roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/shipments/" + parcel.getShipmentId()).with(user(stranger.getUsername()).roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/cod-collections/courier/" + first.getCourierId()).with(user(second.getUser().getUsername()).roles("COURIER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/parcels").with(user("receptionist").roles("RECEPTIONIST"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/shipments").with(user("receptionist").roles("RECEPTIONIST"))).andExpect(status().isOk());
    }

    @Test
    void trackingContainsOnlyPublicFields() throws Exception {
        ParcelDTO parcel = book(true);
        mvc.perform(get("/api/parcels/track/" + parcel.getTrackingNumber()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.body.trackingNumber").value(parcel.getTrackingNumber()))
                .andExpect(jsonPath("$.body.customerId").doesNotExist()).andExpect(jsonPath("$.body.pickupAddress").doesNotExist())
                .andExpect(jsonPath("$.body.codAmount").doesNotExist()).andExpect(jsonPath("$.body.parcelId").doesNotExist());
    }

    @Test
    void transferClearsAssignmentAndRejectsAnotherCouriersActions() {
        ParcelDTO parcel = book(true); login(first.getUser()); parcelService.claim(parcel.getParcelId());
        login(second.getUser()); assertThrows(CustomException.class, () -> changeStatus(parcel.getParcelId(), ParcelStatus.IN_TRANSIT));
        login(first.getUser());
        transferService.create(new BranchTransferDTO(null, parcel.getParcelId(), branch.getBranchId(), otherBranch.getBranchId(), null));
        assertNull(parcels.findById(parcel.getParcelId()).orElseThrow().getAssignedCourier());
        assertThrows(CustomException.class, () -> parcelService.claim(parcel.getParcelId()));
    }

    @Test
    void auditAndNotificationsFollowSuccessfulChangesOnly() {
        ParcelDTO parcel = book(false);
        long before = audit.count();
        login(second.getUser()); assertThrows(CustomException.class, () -> changeStatus(parcel.getParcelId(), ParcelStatus.DELIVERED));
        assertEquals(before, audit.count());
        deliver(parcel);
        login(admin); receipt(invoice(parcel), "100.00", UUID.randomUUID().toString());
        assertTrue(audit.count() > before);
        assertTrue(notifications.findByUser_UserId(owner.getUserId()).stream().anyMatch(n -> n.getMessage().startsWith("Received")));
        assertTrue(notifications.findByUser_UserId(first.getUser().getUserId()).stream().anyMatch(n -> n.getMessage().contains("assigned")));
        login(stranger); assertThrows(CustomException.class, () -> notificationService.getByUserId(owner.getUserId()));
    }

    @Test
    void rejectsOverlapsInvoicedAttachmentsAndInvalidOnlineBookings() {
        assertThrows(CustomException.class, () -> pricingService.create(new PricingRuleDTO(null, zone.getZoneId(), new BigDecimal("20"), "10-20", true)));
        ParcelDTO parcel = book(true); ParcelDTO dto = booking(true); dto.setShipmentId(parcel.getShipmentId());
        assertThrows(CustomException.class, () -> parcelService.createForCustomer(customer.getCustomerId(), dto));
        login(owner); assertThrows(CustomException.class, () -> parcelService.createMine(owner.getUsername(), dto));
        assertThrows(CustomException.class, () -> parcelService.createMine(owner.getUsername(), booking(false)));
        login(admin); assertThrows(CustomException.class, () -> parcelService.delete(parcel.getParcelId()));
    }

    @Test
    void weeklyBoundaryUsesMondayInColombo() {
        assertEquals(LocalDateTime.of(2026, 10, 19, 0, 0), ChatbotServiceImpl.weekStart(Instant.parse("2026-10-18T18:30:00Z")));
        assertEquals(LocalDateTime.of(2026, 10, 12, 0, 0), ChatbotServiceImpl.weekStart(Instant.parse("2026-10-18T18:29:59Z")));
    }

    @Test
    void malformedOperationalRequestsReturn400() throws Exception {
        for (String endpoint : List.of("/api/vehicles", "/api/couriers", "/api/notification", "/api/invoices", "/api/tracking-events")) {
            mvc.perform(post(endpoint).with(user(admin.getUsername()).roles("ADMIN"))
                    .contentType("application/json").content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void historicalPaymentsBlockNewReceiptsWithoutChangingOldValues() {
        ParcelDTO parcel = book(false);
        InvoiceDTO invoice = invoice(parcel);
        Payment legacy = new Payment();
        Invoice reference = new Invoice(); reference.setInvoiceId(invoice.getInvoiceId());
        legacy.setInvoice(reference); legacy.setAmount(new BigDecimal("100.00"));
        legacy.setMethod("PREPAID"); legacy.setPaidAt(LocalDateTime.of(2026, 9, 1, 0, 0));
        legacy = payments.save(legacy);
        assertThrows(CustomException.class, () -> receipt(invoice, "1.00", UUID.randomUUID().toString()));
        Payment saved = payments.findById(legacy.getPaymentId()).orElseThrow();
        assertNull(saved.getRequestReference());
        assertEquals(0, saved.getAmount().compareTo(new BigDecimal("100.00")));
    }

    @Test
    void weeklyReceiptQueryUsesHalfOpenBoundariesAndExcludesUnreviewedPayments() {
        InvoiceDTO invoice = invoice(book(false));
        LocalDateTime monday = LocalDateTime.of(2026, 10, 12, 0, 0);
        BigDecimal before = payments.sumReceiptsBetween(monday, monday.plusWeeks(1));
        for (int i = 0; i < 4; i++) {
            Payment receipt = new Payment(); Invoice reference = new Invoice(); reference.setInvoiceId(invoice.getInvoiceId());
            receipt.setInvoice(reference); receipt.setMethod("CASH"); receipt.setAmount(new BigDecimal("10.00"));
            receipt.setRequestReference(i == 3 ? null : UUID.randomUUID().toString());
            receipt.setPaidAt(i == 0 ? monday.minusSeconds(1) : i == 2 ? monday.plusWeeks(1) : monday);
            payments.save(receipt);
        }
        assertEquals(0, payments.sumReceiptsBetween(monday, monday.plusWeeks(1)).subtract(before).compareTo(new BigDecimal("10.00")));
    }

    private List<Object> race(User a, User b, Supplier<?> one, Supplier<?> two) throws Exception {
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            var firstResult = pool.submit(() -> concurrent(a, one, ready, start));
            var secondResult = pool.submit(() -> concurrent(b, two, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS)); start.countDown();
            return List.of(firstResult.get(20, TimeUnit.SECONDS), secondResult.get(20, TimeUnit.SECONDS));
        }
    }

    private Object concurrent(User user, Supplier<?> action, CountDownLatch ready, CountDownLatch start) throws Exception {
        login(user); ready.countDown(); start.await();
        try { return action.get(); } catch (RuntimeException error) { return error; }
        finally { SecurityContextHolder.clearContext(); }
    }
}
