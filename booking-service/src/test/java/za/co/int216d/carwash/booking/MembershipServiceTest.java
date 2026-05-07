package za.co.int216d.carwash.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.int216d.carwash.booking.membership.domain.Membership;
import za.co.int216d.carwash.booking.membership.domain.MembershipPlan;
import za.co.int216d.carwash.booking.membership.dto.SubscribeMembershipRequest;
import za.co.int216d.carwash.booking.membership.repository.MembershipCreditLogRepository;
import za.co.int216d.carwash.booking.membership.repository.MembershipPlanRepository;
import za.co.int216d.carwash.booking.membership.repository.MembershipRepository;
import za.co.int216d.carwash.booking.membership.service.MembershipService;
import za.co.int216d.carwash.booking.notification.producer.MembershipEventProducer;
import za.co.int216d.carwash.booking.payment.domain.PaymentStatus;
import za.co.int216d.carwash.booking.payment.dto.PaymentProcessResult;
import za.co.int216d.carwash.booking.payment.dto.PaymentRequest;
import za.co.int216d.carwash.booking.payment.service.PaymentService;
import za.co.int216d.carwash.common.exception.BadRequestException;
import za.co.int216d.carwash.common.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MembershipService
 */
@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private MembershipPlanRepository planRepository;

    @Mock
    private MembershipCreditLogRepository creditLogRepository;

    @Mock
    private MembershipEventProducer eventProducer;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private MembershipService membershipService;

    private MembershipPlan testPlan;
    private Membership testMembership;

    @BeforeEach
    void setUp() {
        testPlan = MembershipPlan.builder()
            .id(1L)
            .name("Premium")
            .monthlyPrice(BigDecimal.valueOf(299.99))
            .creditsPerMonth(40)
            .freeWashes(5)
            .isActive(true)
            .discountPercentage(BigDecimal.valueOf(10.0))
            .build();

        LocalDateTime now = LocalDateTime.now();
        testMembership = Membership.builder()
            .id(1L)
            .clientId(100L)
            .plan(testPlan)
            .status(Membership.MembershipStatus.ACTIVE)
            .startDate(now)
            .expiryDate(now.plusMonths(1))
            .creditsRemaining(40)
            .washesUsedThisMonth(0)
            .autoRenew(true)
            .build();
    }

    @Test
    void testSubscribeToPlan_Success() {
        Long clientId = 100L;
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(1L)
            .autoRenew(true)
            .payment(PaymentRequest.builder().paymentMethodToken("tok_test").build())
            .build();
        PaymentProcessResult paymentResult = PaymentProcessResult.builder()
            .transactionId(1L)
            .reference("PAY-TEST")
            .status(PaymentStatus.SUCCEEDED)
            .build();

        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.empty());
        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(paymentService.processPayment(anyLong(), any(), any(), any(), any(), any(), any(), any())).thenReturn(paymentResult);
        when(membershipRepository.save(any())).thenReturn(testMembership);
        when(creditLogRepository.save(any())).thenReturn(null);

        var response = membershipService.subscribeToPlan(clientId, request);

        assertNotNull(response);
        assertEquals(100L, response.getClientId());
        assertEquals("Premium", response.getPlan().getName());
        assertEquals("ACTIVE", response.getStatus());

        verify(membershipRepository, times(1)).findByClientId(clientId);
        verify(planRepository, times(1)).findById(1L);
        verify(eventProducer, times(1)).publishSubscriptionEvent(eq(clientId), eq(1L), eq("Premium"), isNull(), isNull());
    }

    @Test
    void testSubscribeToPlan_AlreadyHasActiveMembership() {
        Long clientId = 100L;
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(1L)
            .autoRenew(true)
            .payment(PaymentRequest.builder().paymentMethodToken("tok_test").build())
            .build();

        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));

        assertThrows(BadRequestException.class, () -> membershipService.subscribeToPlan(clientId, request));

        verify(membershipRepository, times(1)).findByClientId(clientId);
    }

    @Test
    void testSubscribeToPlan_PlanNotFound() {
        Long clientId = 100L;
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(999L)
            .autoRenew(true)
            .payment(PaymentRequest.builder().paymentMethodToken("tok_test").build())
            .build();

        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.empty());
        when(planRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> membershipService.subscribeToPlan(clientId, request));
    }

    @Test
    void testSubscribeToPlan_InactivePlan() {
        Long clientId = 100L;
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(1L)
            .autoRenew(true)
            .payment(PaymentRequest.builder().paymentMethodToken("tok_test").build())
            .build();

        testPlan.setIsActive(false);

        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.empty());
        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));

        assertThrows(BadRequestException.class, () -> membershipService.subscribeToPlan(clientId, request));
    }

    @Test
    void testCancelMembership_Success() {
        Long clientId = 100L;

        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));
        when(membershipRepository.save(any())).thenReturn(testMembership);

        membershipService.cancelMembership(clientId);

        verify(membershipRepository, times(1)).findByClientId(clientId);
        verify(membershipRepository, times(1)).save(any());
        verify(eventProducer, times(1)).publishCancellationEvent(
            eq(clientId), eq(testPlan.getId()), eq(testPlan.getName()), isNull(), isNull());
    }

    @Test
    void testSuspendMembership_Success() {
        Long clientId = 100L;

        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));
        when(membershipRepository.save(any())).thenReturn(testMembership);

        membershipService.suspendMembership(clientId);

        verify(membershipRepository, times(1)).findByClientId(clientId);
        verify(membershipRepository, times(1)).save(any());
        verify(eventProducer, times(1)).publishSuspensionEvent(
            eq(clientId), eq(testPlan.getId()), eq(testPlan.getName()), isNull(), isNull());
    }

    @Test
    void testDeductCredits_Success() {
        Long clientId = 100L;
        testMembership.setCreditsRemaining(40);

        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));
        when(membershipRepository.save(any())).thenReturn(testMembership);

        membershipService.deductCredits(clientId, 10, "Booking #123");

        assertEquals(30, testMembership.getCreditsRemaining());
        verify(membershipRepository, times(1)).save(any());
        verify(creditLogRepository, times(1)).save(any());
    }

    @Test
    void testDeductCredits_InsufficientCredits() {
        Long clientId = 100L;
        testMembership.setCreditsRemaining(5);

        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));

        assertThrows(BadRequestException.class, () -> membershipService.deductCredits(clientId, 10, "Booking #123"));
    }

    @Test
    void testAddCredits_Success() {
        Long clientId = 100L;
        testMembership.setCreditsRemaining(40);

        when(membershipRepository.findByClientId(clientId)).thenReturn(Optional.of(testMembership));
        when(membershipRepository.save(any())).thenReturn(testMembership);

        membershipService.addCredits(clientId, 20, "Purchase");

        assertEquals(60, testMembership.getCreditsRemaining());
        verify(membershipRepository, times(1)).save(any());
        verify(creditLogRepository, times(1)).save(any());
    }
}
