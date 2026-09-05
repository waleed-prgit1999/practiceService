package com.example.travel.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI travelPlatformOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Travel Management Platform API")
                        .description(
                                "Phase 1 API for user accounts, trips, travelers, flight segments, hotel bookings, "
                                        + "notifications and audit history. All endpoints except /api/v1/auth/** "
                                        + "require a Bearer access token. Errors follow a single JSON shape "
                                        + "(timestamp, status, error, message, path, details).")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
