package com.booking.app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "FindMeTicket API", version = "1.0",
        description = "FindMeTicket service API documentation ",
        contact = @Contact(email = "mishaakamichael999@gmail.com", name = "Mykhailo Marchuk"))
)
public class SwaggerConfig {
}
