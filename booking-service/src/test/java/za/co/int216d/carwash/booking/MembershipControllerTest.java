package za.co.int216d.carwash.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import za.co.int216d.carwash.booking.membership.web.MembershipController;
import za.co.int216d.carwash.booking.membership.dto.MembershipDetailResponse;
import za.co.int216d.carwash.booking.membership.dto.MembershipPaymentRequest;
import za.co.int216d.carwash.booking.membership.dto.MembershipPlanResponse;
import za.co.int216d.carwash.booking.membership.dto.SubscribeMembershipRequest;
import za.co.int216d.carwash.booking.membership.service.MembershipService;
import za.co.int216d.carwash.common.security.SecurityUtils;
import za.co.int216d.carwash.booking.payment.dto.PaymentRequest;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for Membership endpoints
 */
@ExtendWith(MockitoExtension.class)
class MembershipControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MembershipService membershipService;

    @Mock
    private SecurityUtils securityUtils;

    private MembershipDetailResponse mockResponse;

    @BeforeEach
    void setUp() {
        MembershipController controller = new MembershipController(membershipService, securityUtils);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        MembershipPlanResponse planResponse = MembershipPlanResponse.builder()
            .id(1L)
            .name("Premium")
            .monthlyPrice(BigDecimal.valueOf(299.99))
            .creditsPerMonth(40)
            .freeWashes(5)
            .discountPercentage(BigDecimal.valueOf(10.0))
            .build();

        mockResponse = MembershipDetailResponse.builder()
            .id(1L)
            .clientId(100L)
            .plan(planResponse)
            .status("ACTIVE")
            .creditsRemaining(40)
            .washesUsedThisMonth(0)
            .autoRenew(true)
            .startDate(LocalDateTime.now())
            .expiryDate(LocalDateTime.now().plusMonths(1))
            .daysUntilExpiry(30)
            .build();

        when(securityUtils.getCurrentUserIdAsLong()).thenReturn(100L);
    }

    @Test
    void testSubscribeToPlan_Success() throws Exception {
        PaymentRequest payment = PaymentRequest.builder()
            .gateway(null)
            .paymentMethodToken("tok_test")
            .build();
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(1L)
            .autoRenew(true)
            .payment(payment)
            .build();

        when(membershipService.subscribeToPlan(eq(100L), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/membership/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.clientId").value(100))
            .andExpect(jsonPath("$.plan.name").value("Premium"));
    }

    @Test
    void testGetMembership_Success() throws Exception {
        when(membershipService.getClientMembership(100L)).thenReturn(mockResponse);

        mockMvc.perform(get("/membership"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.creditsRemaining").value(40));
    }

    @Test
    void testCancelMembership_Success() throws Exception {
        mockMvc.perform(post("/membership/cancel"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testRenewMembership_Success() throws Exception {
        PaymentRequest payment = PaymentRequest.builder()
            .gateway(null)
            .paymentMethodToken("tok_test")
            .build();
        MembershipPaymentRequest request = MembershipPaymentRequest.builder()
            .payment(payment)
            .build();

        when(membershipService.renewMembership(eq(100L), any(MembershipPaymentRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/membership/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
