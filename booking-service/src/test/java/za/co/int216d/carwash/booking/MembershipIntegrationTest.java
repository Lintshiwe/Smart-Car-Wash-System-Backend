package za.co.int216d.carwash.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import za.co.int216d.carwash.booking.membership.domain.MembershipPlan;
import za.co.int216d.carwash.booking.membership.dto.SubscribeMembershipRequest;
import za.co.int216d.carwash.booking.membership.repository.MembershipPlanRepository;
import za.co.int216d.carwash.booking.membership.repository.MembershipRepository;
import za.co.int216d.carwash.booking.membership.service.MembershipService;

import java.math.BigDecimal;
import za.co.int216d.carwash.booking.payment.dto.PaymentRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Membership module with TestContainers
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.flyway.enabled=false"
})
@Transactional
class MembershipIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private MembershipPlanRepository planRepository;

    private MembershipPlan testPlan;

    @BeforeEach
    void setUp() {
        // Clear repositories
        membershipRepository.deleteAll();
        planRepository.deleteAll();

        // Create test plan
        testPlan = MembershipPlan.builder()
            .name("Test Plan")
            .monthlyPrice(BigDecimal.valueOf(99.99))
            .creditsPerMonth(20)
            .freeWashes(2)
            .isActive(true)
            .discountPercentage(BigDecimal.valueOf(5.0))
            .build();

        testPlan = planRepository.save(testPlan);
    }

    @Test
    void testSubscribeToPlan_Integration() {
        Long clientId = 100L;
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(testPlan.getId())
            .autoRenew(true)
            .payment(PaymentRequest.builder().paymentMethodToken("tok_test").build())
            .build();

        var response = membershipService.subscribeToPlan(clientId, request);

        assertNotNull(response);
        assertEquals(clientId, response.getClientId());
        assertEquals("Test Plan", response.getPlan().getName());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(20, response.getCreditsRemaining());

        // Verify persistence
        assertTrue(membershipRepository.findByClientId(clientId).isPresent());
    }

    @Test
    void testCancelMembership_Integration() {
        Long clientId = 100L;
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(testPlan.getId())
            .autoRenew(true)
            .payment(PaymentRequest.builder().paymentMethodToken("tok_test").build())
            .build();

        membershipService.subscribeToPlan(clientId, request);
        membershipService.cancelMembership(clientId);

        var membership = membershipRepository.findByClientId(clientId);
        assertTrue(membership.isPresent());
        assertEquals("CANCELLED", membership.get().getStatus().toString());
    }

    @Test
    void testGetMembershipDetails_Integration() {
        Long clientId = 100L;
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(testPlan.getId())
            .autoRenew(true)
            .payment(PaymentRequest.builder().paymentMethodToken("tok_test").build())
            .build();

        membershipService.subscribeToPlan(clientId, request);
        var details = membershipService.getClientMembership(clientId);

        assertNotNull(details);
        assertEquals(clientId, details.getClientId());
        assertEquals(testPlan.getId(), details.getPlan().getId());
    }

    @Test
    void testDeductCredits_Integration() {
        Long clientId = 100L;
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(testPlan.getId())
            .autoRenew(true)
            .payment(PaymentRequest.builder().paymentMethodToken("tok_test").build())
            .build();

        membershipService.subscribeToPlan(clientId, request);
        var beforeDeduction = membershipRepository.findByClientId(clientId).get().getCreditsRemaining();

        membershipService.deductCredits(clientId, 5, "Test deduction");

        var afterDeduction = membershipRepository.findByClientId(clientId).get().getCreditsRemaining();
        assertEquals(beforeDeduction - 5, afterDeduction);
    }

    @Test
    void testAddCredits_Integration() {
        Long clientId = 100L;
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(testPlan.getId())
            .autoRenew(true)
            .payment(PaymentRequest.builder().paymentMethodToken("tok_test").build())
            .build();

        membershipService.subscribeToPlan(clientId, request);
        var beforeAddition = membershipRepository.findByClientId(clientId).get().getCreditsRemaining();

        membershipService.addCredits(clientId, 10, "Test addition");

        var afterAddition = membershipRepository.findByClientId(clientId).get().getCreditsRemaining();
        assertEquals(beforeAddition + 10, afterAddition);
    }

    @Test
    void testMembershipPersistence() {
        Long clientId = 101L;
        SubscribeMembershipRequest request = SubscribeMembershipRequest.builder()
            .planId(testPlan.getId())
            .autoRenew(true)
            .payment(PaymentRequest.builder().paymentMethodToken("tok_test").build())
            .build();

        membershipService.subscribeToPlan(clientId, request);

        // Verify database
        var membership = membershipRepository.findByClientId(clientId);
        assertTrue(membership.isPresent());
        assertNotNull(membership.get().getStartDate());
        assertNotNull(membership.get().getExpiryDate());
    }
}
