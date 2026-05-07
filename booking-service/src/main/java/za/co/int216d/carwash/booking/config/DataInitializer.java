package za.co.int216d.carwash.booking.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import za.co.int216d.carwash.booking.catalogue.domain.CatalogueAddon;
import za.co.int216d.carwash.booking.catalogue.domain.CatalogueServiceItem;
import za.co.int216d.carwash.booking.catalogue.repository.CatalogueAddonRepository;
import za.co.int216d.carwash.booking.catalogue.repository.CatalogueServiceRepository;
import za.co.int216d.carwash.booking.membership.domain.MembershipPlan;
import za.co.int216d.carwash.booking.membership.repository.MembershipPlanRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MembershipPlanRepository planRepository;
    private final CatalogueServiceRepository serviceRepository;
    private final CatalogueAddonRepository addonRepository;

    @Override
    public void run(String... args) {
        seedMembershipPlans();
        seedCatalogueServices();
        seedCatalogueAddons();
    }

    private void seedMembershipPlans() {
        if (planRepository.count() > 0) return;

        log.info("Seeding membership plans...");
        planRepository.save(MembershipPlan.builder()
            .name("Basic")
            .description("Basic membership with limited credits and benefits")
            .monthlyPrice(new BigDecimal("99.99"))
            .creditsPerMonth(10)
            .freeWashes(0)
            .isActive(true)
            .discountEligible(false)
            .discountPercentage(BigDecimal.ZERO)
            .build());

        planRepository.save(MembershipPlan.builder()
            .name("Standard")
            .description("Standard membership with monthly credits and 2 free washes")
            .monthlyPrice(new BigDecimal("199.99"))
            .creditsPerMonth(20)
            .freeWashes(2)
            .isActive(true)
            .discountEligible(true)
            .discountPercentage(new BigDecimal("5.00"))
            .build());

        planRepository.save(MembershipPlan.builder()
            .name("Premium")
            .description("Premium membership with high credit allocation and benefits")
            .monthlyPrice(new BigDecimal("299.99"))
            .creditsPerMonth(40)
            .freeWashes(5)
            .isActive(true)
            .discountEligible(true)
            .discountPercentage(new BigDecimal("10.00"))
            .build());

        planRepository.save(MembershipPlan.builder()
            .name("VIP")
            .description("VIP membership with maximum credits and exclusive perks")
            .monthlyPrice(new BigDecimal("499.99"))
            .creditsPerMonth(80)
            .freeWashes(10)
            .isActive(true)
            .discountEligible(true)
            .discountPercentage(new BigDecimal("15.00"))
            .build());

        log.info("Membership plans seeded.");
    }

    private void seedCatalogueServices() {
        if (serviceRepository.count() > 0) return;

        log.info("Seeding catalogue services...");
        serviceRepository.save(createService("BASIC_WASH", "Basic Exterior Wash",
            "Quick hand wash and rinse for your vehicle", new BigDecimal("99.00"), "EXTERIOR"));
        serviceRepository.save(createService("DELUXE_WASH", "Deluxe Exterior Wash",
            "Premium hand wash with foam cannon and tire shine", new BigDecimal("199.00"), "EXTERIOR"));
        serviceRepository.save(createService("PREMIUM_WASH", "Premium Complete Wash",
            "Full exterior and interior clean with vacuum", new BigDecimal("349.00"), "FULL"));
        serviceRepository.save(createService("WAX_POLISH", "Wax & Polish Treatment",
            "Professional-grade wax application and buffing", new BigDecimal("499.00"), "EXTERIOR"));
        serviceRepository.save(createService("CERAMIC_COAT", "Ceramic Coating",
            "Ceramic protection coating application", new BigDecimal("1499.00"), "PROTECTION"));
        serviceRepository.save(createService("MOBILE_WASH", "Mobile Wash Service",
            "We come to you for a complete wash", new BigDecimal("449.00"), "MOBILE"));
        log.info("Catalogue services seeded.");
    }

    private void seedCatalogueAddons() {
        if (addonRepository.count() > 0) return;

        log.info("Seeding catalogue addons...");
        addonRepository.save(createAddon("TIRE_SHINE", "Tire Shine Treatment",
            new BigDecimal("49.00")));
        addonRepository.save(createAddon("LEATHER_CARE", "Leather Care Treatment",
            new BigDecimal("99.00")));
        addonRepository.save(createAddon("ENGINE_CLEAN", "Engine Bay Cleaning",
            new BigDecimal("149.00")));
        log.info("Catalogue addons seeded.");
    }

    private CatalogueServiceItem createService(String code, String name, String description,
                                                BigDecimal basePrice, String type) {
        return CatalogueServiceItem.builder()
            .code(code)
            .name(name)
            .description(description)
            .serviceType(type)
            .basePrice(basePrice)
            .durationMinutes(30)
            .isActive(true)
            .build();
    }

    private CatalogueAddon createAddon(String code, String name, BigDecimal price) {
        return CatalogueAddon.builder()
            .code(code)
            .name(name)
            .price(price)
            .isActive(true)
            .build();
    }
}
