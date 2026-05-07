package za.co.int216d.carwash.booking.membership.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.int216d.carwash.booking.membership.domain.MembershipCreditLog;
import za.co.int216d.carwash.booking.membership.dto.CreditTransactionResponse;
import za.co.int216d.carwash.booking.membership.service.MembershipCreditService;
import za.co.int216d.carwash.common.security.SecurityUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for membership credit transactions
 */
@RestController
@RequestMapping("/membership/credits")
@Slf4j
public class MembershipCreditController {

    private final MembershipCreditService creditService;
    private final SecurityUtils securityUtils;

    public MembershipCreditController(
        MembershipCreditService creditService,
        SecurityUtils securityUtils
    ) {
        this.creditService = creditService;
        this.securityUtils = securityUtils;
    }

    /**
     * Get client's credit transaction history (paginated)
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<CreditTransactionResponse>> getCreditHistory(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("GET /membership/credits/history - Fetching credit history for client {}", clientId);
        Page<CreditTransactionResponse> history = creditService.getClientCreditHistory(clientId, pageable);
        return ResponseEntity.ok(history);
    }

    /**
     * Get credit transactions within a date range
     */
    @GetMapping("/history/date-range")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<CreditTransactionResponse>> getCreditHistoryByDateRange(
        @RequestParam LocalDateTime startDate,
        @RequestParam LocalDateTime endDate
    ) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("GET /membership/credits/history/date-range - Fetching credit history for client {} between {} and {}",
            clientId, startDate, endDate);
        List<CreditTransactionResponse> history = creditService.getClientCreditHistoryByDateRange(clientId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    /**
     * Get credit transactions by type
     */
    @GetMapping("/history/type/{type}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<CreditTransactionResponse>> getCreditTransactionsByType(
        @PathVariable String type
    ) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        log.info("GET /membership/credits/history/type/{} - Fetching {} transactions for client {}", type, type, clientId);

        MembershipCreditLog.TransactionType transactionType = MembershipCreditLog.TransactionType.valueOf(type.toUpperCase());
        List<CreditTransactionResponse> transactions = creditService.getClientCreditTransactionsByType(clientId, transactionType);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get credit summary/analytics for a date range
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> getCreditSummary(
        @RequestParam(required = false) LocalDateTime startDate,
        @RequestParam(required = false) LocalDateTime endDate
    ) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();

        // Default to last month if dates not provided
        LocalDateTime actualEndDate = endDate != null ? endDate : LocalDateTime.now();
        LocalDateTime actualStartDate = startDate != null ? startDate : actualEndDate.minusMonths(1);

        log.info("GET /membership/credits/summary - Fetching credit summary for client {}", clientId);

        Integer totalPurchased = creditService.getTotalCreditsPurchased(clientId, actualStartDate, actualEndDate);
        Integer totalUsed = creditService.getTotalCreditsUsed(clientId, actualStartDate, actualEndDate);

        Map<String, Object> summary = new HashMap<>();
        summary.put("period", Map.of("startDate", actualStartDate, "endDate", actualEndDate));
        summary.put("totalCreditsPurchased", totalPurchased);
        summary.put("totalCreditsUsed", totalUsed);
        summary.put("netChange", totalPurchased - totalUsed);

        return ResponseEntity.ok(summary);
    }
}
