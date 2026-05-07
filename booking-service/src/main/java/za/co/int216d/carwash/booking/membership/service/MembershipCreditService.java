package za.co.int216d.carwash.booking.membership.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.booking.membership.domain.MembershipCreditLog;
import za.co.int216d.carwash.booking.membership.dto.CreditTransactionResponse;
import za.co.int216d.carwash.booking.membership.repository.MembershipCreditLogRepository;
import za.co.int216d.carwash.booking.membership.repository.MembershipRepository;
import za.co.int216d.carwash.common.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing membership credit transactions
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class MembershipCreditService {

    private final MembershipCreditLogRepository creditLogRepository;
    private final MembershipRepository membershipRepository;

    public MembershipCreditService(
        MembershipCreditLogRepository creditLogRepository,
        MembershipRepository membershipRepository
    ) {
        this.creditLogRepository = creditLogRepository;
        this.membershipRepository = membershipRepository;
    }

    /**
     * Get paginated credit transaction history for a client
     */
    public Page<CreditTransactionResponse> getClientCreditHistory(Long clientId, Pageable pageable) {
        // Verify membership exists
        membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId));

        Long membershipId = membershipRepository.findByClientId(clientId).get().getId();

        return creditLogRepository.findByMembershipIdOrderByCreatedAtDesc(membershipId, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get credit transactions within a date range
     */
    public List<CreditTransactionResponse> getClientCreditHistoryByDateRange(
        Long clientId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        Long membershipId = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId))
            .getId();

        return creditLogRepository.findByMembershipIdAndCreatedAtBetween(membershipId, startDate, endDate)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get credit transactions of a specific type
     */
    public List<CreditTransactionResponse> getClientCreditTransactionsByType(
        Long clientId,
        MembershipCreditLog.TransactionType type
    ) {
        Long membershipId = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId))
            .getId();

        return creditLogRepository.findByMembershipIdAndType(membershipId, type)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get total credits purchased in a period
     */
    public Integer getTotalCreditsPurchased(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        Long membershipId = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId))
            .getId();

        return creditLogRepository.findByMembershipIdAndCreatedAtBetween(membershipId, startDate, endDate)
            .stream()
            .filter(log -> log.getType() == MembershipCreditLog.TransactionType.PURCHASE)
            .mapToInt(MembershipCreditLog::getCreditsChanged)
            .sum();
    }

    /**
     * Get total credits used in a period
     */
    public Integer getTotalCreditsUsed(Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        Long membershipId = membershipRepository.findByClientId(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("No membership found for client ID: " + clientId))
            .getId();

        return creditLogRepository.findByMembershipIdAndCreatedAtBetween(membershipId, startDate, endDate)
            .stream()
            .filter(log -> log.getType() == MembershipCreditLog.TransactionType.USAGE)
            .mapToInt(log -> Math.abs(log.getCreditsChanged()))
            .sum();
    }

    private CreditTransactionResponse mapToResponse(MembershipCreditLog log) {
        return CreditTransactionResponse.builder()
            .id(log.getId())
            .type(log.getType().toString())
            .creditsChanged(log.getCreditsChanged())
            .balanceBefore(log.getBalanceBefore())
            .balanceAfter(log.getBalanceAfter())
            .description(log.getDescription())
            .createdAt(log.getCreatedAt())
            .build();
    }
}
