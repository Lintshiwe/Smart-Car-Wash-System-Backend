package za.co.int216d.carwash.client.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.int216d.carwash.client.domain.Address;
import za.co.int216d.carwash.client.domain.Client;
import za.co.int216d.carwash.client.domain.Mandate;
import za.co.int216d.carwash.client.domain.Vehicle;
import za.co.int216d.carwash.client.dto.AcceptMandateRequest;
import za.co.int216d.carwash.client.dto.AddressDto;
import za.co.int216d.carwash.client.dto.ClientProfileDto;
import za.co.int216d.carwash.client.dto.CreateAddressRequest;
import za.co.int216d.carwash.client.dto.CreateVehicleRequest;
import za.co.int216d.carwash.client.dto.MandateDto;
import za.co.int216d.carwash.client.dto.UpsertProfileRequest;
import za.co.int216d.carwash.client.dto.VehicleDto;
import za.co.int216d.carwash.client.repository.AddressRepository;
import za.co.int216d.carwash.client.repository.ClientRepository;
import za.co.int216d.carwash.client.repository.MandateRepository;
import za.co.int216d.carwash.client.repository.VehicleRepository;
import za.co.int216d.carwash.common.exception.ApiException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clients;
    private final VehicleRepository vehicles;
    private final AddressRepository addresses;
    private final MandateRepository mandates;

    @Transactional(readOnly = true)
    public ClientProfileDto getProfile(UUID userId) {
        Client c = clients.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Profile not found. Please complete registration."));
        return toProfile(c);
    }

    @Transactional
    public ClientProfileDto upsertProfile(UUID userId, UpsertProfileRequest req) {
        Client c = clients.findByUserId(userId).orElseGet(() -> Client.builder().userId(userId).build());
        c.setFirstName(req.firstName());
        c.setLastName(req.lastName());
        c.setPhone(req.phone());
        c.setIdNumber(req.idNumber());
        c.setDateOfBirth(req.dateOfBirth());
        return toProfile(clients.save(c));
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> listVehicles(UUID userId) {
        UUID clientId = requireClient(userId).getId();
        return vehicles.findByClientIdOrderByCreatedAtDesc(clientId).stream().map(this::toVehicleDto).toList();
    }

    @Transactional
    public VehicleDto addVehicle(UUID userId, CreateVehicleRequest req) {
        Client c = requireClient(userId);
        String plate = req.licensePlate().trim().toUpperCase();
        if (vehicles.existsByClientIdAndLicensePlateIgnoreCase(c.getId(), plate)) {
            throw ApiException.conflict("You already have a vehicle with this license plate");
        }
        Vehicle v = Vehicle.builder()
                .clientId(c.getId())
                .make(req.vehicleMake())
                .model(req.vehicleModel())
                .licensePlate(plate)
                .vehicleType(req.vehicleType())
                .build();
        return toVehicleDto(vehicles.save(v));
    }

    @Transactional
    public void removeVehicle(UUID userId, UUID vehicleId) {
        Client c = requireClient(userId);
        Vehicle v = vehicles.findByIdAndClientId(vehicleId, c.getId())
                .orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        vehicles.delete(v);
    }

    @Transactional(readOnly = true)
    public List<AddressDto> listAddresses(UUID userId) {
        UUID clientId = requireClient(userId).getId();
        return addresses.findByClientIdOrderByCreatedAtDesc(clientId).stream().map(this::toAddressDto).toList();
    }

    @Transactional
    public AddressDto addAddress(UUID userId, CreateAddressRequest req) {
        Client c = requireClient(userId);
        boolean makePrimary = Boolean.TRUE.equals(req.primary()) || addresses.countByClientId(c.getId()) == 0;
        Address a = Address.builder()
                .clientId(c.getId())
                .street(req.street())
                .city(req.city())
                .province(req.province())
                .postalCode(req.postalCode())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .primary(makePrimary)
                .build();
        return toAddressDto(addresses.save(a));
    }

    @Transactional
    public MandateDto acceptMandate(UUID userId, AcceptMandateRequest req, HttpServletRequest request) {
        Client c = requireClient(userId);
        Mandate m = Mandate.builder()
                .clientId(c.getId())
                .mandateVersion(req.mandateVersion())
                .agreedFullName(req.agreedFullName())
                .ipAddress(extractIp(request))
                .pdfUrl(null)
                .build();
        return toMandateDto(mandates.save(m));
    }

    @Transactional(readOnly = true)
    public List<MandateDto> listMandates(UUID userId) {
        UUID clientId = requireClient(userId).getId();
        return mandates.findByClientIdOrderByAcceptedAtDesc(clientId).stream().map(this::toMandateDto).toList();
    }

    private Client requireClient(UUID userId) {
        return clients.findByUserId(userId)
                .orElseThrow(() -> ApiException.badRequest("Complete your profile before performing this action"));
    }

    private static String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private ClientProfileDto toProfile(Client c) {
        return new ClientProfileDto(c.getId(), c.getUserId(), c.getFirstName(), c.getLastName(),
                c.getPhone(), c.getIdNumber(), c.getDateOfBirth(), c.getCreatedAt());
    }

    private VehicleDto toVehicleDto(Vehicle v) {
        return new VehicleDto(v.getId(), v.getClientId(), v.getMake(), v.getModel(),
                v.getLicensePlate(), v.getVehicleType(), v.getCreatedAt());
    }

    private AddressDto toAddressDto(Address a) {
        return new AddressDto(a.getId(), a.getClientId(), a.getStreet(), a.getCity(), a.getProvince(),
                a.getPostalCode(), a.getLatitude(), a.getLongitude(), a.isPrimary(), a.getCreatedAt());
    }

    private MandateDto toMandateDto(Mandate m) {
        return new MandateDto(m.getId(), m.getClientId(), m.getMandateVersion(),
                m.getAgreedFullName(), m.getPdfUrl(), m.getAcceptedAt());
    }
}
