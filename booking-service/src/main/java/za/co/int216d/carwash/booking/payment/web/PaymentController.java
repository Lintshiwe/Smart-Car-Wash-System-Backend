package za.co.int216d.carwash.booking.payment.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.int216d.carwash.booking.payment.dto.PaymentTransactionResponse;
import za.co.int216d.carwash.booking.payment.service.PaymentService;
import za.co.int216d.carwash.common.security.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<PaymentTransactionResponse>> myTransactions() {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        return ResponseEntity.ok(paymentService.getClientTransactions(clientId));
    }

    @GetMapping("/me/{reference}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentTransactionResponse> myTransactionByReference(@PathVariable String reference) {
        Long clientId = securityUtils.getCurrentUserIdAsLong();
        return ResponseEntity.ok(paymentService.getClientTransactionByReference(clientId, reference));
    }
}
