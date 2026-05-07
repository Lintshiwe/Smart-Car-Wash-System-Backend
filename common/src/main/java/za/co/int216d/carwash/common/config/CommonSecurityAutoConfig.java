package za.co.int216d.carwash.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import za.co.int216d.carwash.common.security.JwtProperties;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
@ComponentScan(basePackages = {
        "za.co.int216d.carwash.common.security",
        "za.co.int216d.carwash.common.exception"
})
public class CommonSecurityAutoConfig {
}
