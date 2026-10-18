package com.ijse.codlink;

import com.ijse.codlink.dto.*;
import com.ijse.codlink.entity.*;
import com.ijse.codlink.enumeration.*;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.*;
import com.ijse.codlink.service.*;
import com.ijse.codlink.service.impl.PricingRuleServiceImpl;
import com.ijse.codlink.security.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewTests {
    @Test
    void rejectsInactiveCustomersAndInvalidWeights() {
        Customer customer = new Customer();
        customer.setStatus(UserStatus.INACTIVE);
        assertThrows(CustomException.class, () -> BookingValidation.validate(customer, new ParcelDTO(), false));
        customer.setStatus(UserStatus.ACTIVE);
        for (double weight : new double[]{0, -1, 71, Double.NaN, Double.POSITIVE_INFINITY}) {
            ParcelDTO dto = new ParcelDTO();
            dto.setOriginBranchId(1L);
            dto.setWeight(weight);
            assertThrows(CustomException.class, () -> BookingValidation.validate(customer, dto, false));
        }
    }

    @Test
    void onlineBookingsRejectSuppliedShipmentAndMissingCod() {
        Customer customer = new Customer();
        customer.setStatus(UserStatus.ACTIVE);
        ParcelDTO dto = new ParcelDTO();
        dto.setOriginBranchId(1L);
        dto.setWeight(2.0);
        assertThrows(CustomException.class, () -> BookingValidation.validate(customer, dto, true));
        dto.setCodAmount(new BigDecimal("100.00"));
        dto.setShipmentId(9L);
        assertThrows(CustomException.class, () -> BookingValidation.validate(customer, dto, true));
    }

    @Test
    void parsesOnlyValidPricingBounds() {
        assertArrayEquals(new double[]{0, 5}, PricingRuleServiceImpl.bracketBounds("0-5"));
        assertEquals(Double.POSITIVE_INFINITY, PricingRuleServiceImpl.bracketBounds("5+")[1]);
        for (String bracket : new String[]{"5-5", "10-5", "-1-5", "NaN+", "bad"}) {
            assertThrows(CustomException.class, () -> PricingRuleServiceImpl.bracketBounds(bracket));
        }
    }

    @Test
    void deliveryRequiresAssignmentAndRecordsEvent() {
        ParcelRepository parcels = mock(ParcelRepository.class);
        TrackingEventRepository events = mock(TrackingEventRepository.class);
        ShipmentService shipments = mock(ShipmentService.class);
        ParcelLifecycle lifecycle = new ParcelLifecycle(parcels, events, shipments, mock(PricingRuleService.class));
        Parcel parcel = new Parcel();
        parcel.setStatus(ParcelStatus.IN_TRANSIT);
        assertThrows(CustomException.class, () -> lifecycle.transition(parcel, ParcelStatus.DELIVERED));
        Courier courier = new Courier();
        parcel.setAssignedCourier(courier);
        when(parcels.save(parcel)).thenReturn(parcel);
        assertSame(parcel, lifecycle.transition(parcel, ParcelStatus.DELIVERED));
        assertSame(courier, parcel.getDeliveredBy());
        verify(events).save(argThat(e -> e.getStatus().equals("DELIVERED") && e.getParcel() == parcel));
        assertThrows(CustomException.class, () -> lifecycle.transition(parcel, ParcelStatus.IN_TRANSIT));
    }

    @Test
    void malformedJwtReturnsHttp401() throws Exception {
        JwtUtil jwt = mock(JwtUtil.class);
        when(jwt.extractUsername("bad")).thenThrow(new io.jsonwebtoken.MalformedJwtException("bad"));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, mock(UserDetailsService.class), new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        assertEquals(401, response.getStatus());
    }

    @Test
    void doesNotConvertApplicationFailureToAuthenticationFailure() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(JwtUtil.class), mock(UserDetailsService.class), new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        assertThrows(IllegalStateException.class, () -> filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> { throw new IllegalStateException("application failure"); }));
    }
}
