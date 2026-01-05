package com.shopzone.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "ShopZone E-Commerce API",
        version = "1.1.0",
        description = """
                        ShopZone E-Commerce Platform API Documentation
                        
                        ## Phase 1 - Week 2: Product Catalog
                        
                        ### Features:
                        - **Authentication** (Week 1): JWT-based user authentication
                        - **Product Catalog** (Week 2): Product and category management with Cloudinary image upload
                        
                        ### Authentication:
                        1. Register a new user or login
                        2. Copy the `accessToken` from the response
                        3. Click **Authorize** and paste the token (without "Bearer")
                        4. Admin endpoints require ROLE_ADMIN
                        
                        ### Public Endpoints (No Auth Required):
                        - GET /api/categories/** - Browse categories
                        - GET /api/products/** - Browse products
                        
                        ### Admin Endpoints (Require ROLE_ADMIN):
                        - POST/PUT/DELETE /api/categories/**
                        - POST/PUT/DELETE /api/products/**
                        - POST /api/products/{id}/images
                        """,
        contact = @Contact(
            name = "ShopZone Support",
            email = "support@shopzone.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Development Server")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Authentication - Enter ONLY the token (without 'Bearer' prefix)",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
  // Configuration is done via annotations
}