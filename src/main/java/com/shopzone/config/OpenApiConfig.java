package com.shopzone.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "ShopZone E-Commerce API",
        version = "1.0.0",
        description = """
            ShopZone is a full-featured e-commerce platform API.
            
            ## Features
            - User authentication with JWT
            - Product catalog management
            - Shopping cart functionality
            - Order management
            - Payment processing (Stripe)
            - Admin dashboard
            
            ## Authentication
            Most endpoints require authentication. Include the JWT token in the Authorization header:
            ```
            Authorization: Bearer <your-token>
            ```
            """,
        contact = @Contact(
            name = "ShopZone Support",
            email = "support@shopzone.com",
            url = "https://shopzone.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Development"),
        @Server(url = "https://api.shopzone.com", description = "Production")
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT authentication",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
  // Configuration is done through annotations
}