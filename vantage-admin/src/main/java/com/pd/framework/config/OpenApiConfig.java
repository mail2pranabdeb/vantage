package com.pd.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for the Gateway API.
 * Provides interactive API documentation accessible at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vantageOpenAPI() {
        String bearerScheme = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("Vantage Admin Gateway API")
                .description("REST API documentation for Vantage Admin Platform. " +
                    "All endpoints are routed through the GatewayManagement controller " +
                    "following the Spring Modulith gateway pattern.\n\n" +
                    "**Authentication**: JWT Bearer tokens. " +
                    "Use POST /api/login to obtain a token, then click Authorize (top right) and paste the token value.\n\n" +
                    "**OAuth2**: Google login is also supported via GET /oauth2/authorization/google.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Vantage Admin Team")
                    .email("admin@vantage.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList(bearerScheme))
            .components(new Components()
                .addSecuritySchemes(bearerScheme,
                    new SecurityScheme()
                        .name(bearerScheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter the JWT access token returned from POST /api/login")));
    }
}
