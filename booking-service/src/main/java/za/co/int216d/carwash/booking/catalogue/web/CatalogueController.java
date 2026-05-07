package za.co.int216d.carwash.booking.catalogue.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.int216d.carwash.booking.catalogue.dto.CatalogueAddonResponse;
import za.co.int216d.carwash.booking.catalogue.dto.CatalogueServiceResponse;
import za.co.int216d.carwash.booking.catalogue.service.CatalogueService;

import java.util.List;

@RestController
@RequestMapping("/catalogue")
@RequiredArgsConstructor
public class CatalogueController {

    private final CatalogueService catalogueService;

    @GetMapping("/services")
    public List<CatalogueServiceResponse> services(
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return catalogueService.listServices(activeOnly);
    }

    @GetMapping("/addons")
    public List<CatalogueAddonResponse> addons(
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return catalogueService.listAddons(activeOnly);
    }
}
