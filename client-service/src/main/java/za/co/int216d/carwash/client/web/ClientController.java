package za.co.int216d.carwash.client.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import za.co.int216d.carwash.client.dto.*;
import za.co.int216d.carwash.client.service.ClientService;
import za.co.int216d.carwash.common.security.AuthPrincipal;
import za.co.int216d.carwash.common.web.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientController {

    private final ClientService service;

    @GetMapping
    public ApiResponse<ClientProfileDto> getProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.ok(service.getProfile(principal.userId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClientProfileDto>> createProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpsertProfileRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.ok("Profile created", service.upsertProfile(principal.userId(), request)));
    }

    @PutMapping
    public ApiResponse<ClientProfileDto> updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpsertProfileRequest request) {
        return ApiResponse.ok("Profile updated", service.upsertProfile(principal.userId(), request));
    }

    @GetMapping("/vehicles")
    public ApiResponse<List<VehicleDto>> listVehicles(@AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.ok(service.listVehicles(principal.userId()));
    }

    @PostMapping("/vehicles")
    public ResponseEntity<ApiResponse<VehicleDto>> addVehicle(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateVehicleRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.ok("Vehicle added", service.addVehicle(principal.userId(), request)));
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    public ApiResponse<Void> removeVehicle(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID vehicleId) {
        service.removeVehicle(principal.userId(), vehicleId);
        return ApiResponse.message("Vehicle removed");
    }

    @GetMapping("/addresses")
    public ApiResponse<List<AddressDto>> listAddresses(@AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.ok(service.listAddresses(principal.userId()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<AddressDto>> addAddress(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.ok("Address added", service.addAddress(principal.userId(), request)));
    }

    @PostMapping("/mandate")
    public ResponseEntity<ApiResponse<MandateDto>> acceptMandate(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody AcceptMandateRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(201)
                .body(ApiResponse.ok("Mandate accepted",
                        service.acceptMandate(principal.userId(), request, httpRequest)));
    }

    @GetMapping("/mandate")
    public ApiResponse<List<MandateDto>> listMandates(@AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.ok(service.listMandates(principal.userId()));
    }
}
