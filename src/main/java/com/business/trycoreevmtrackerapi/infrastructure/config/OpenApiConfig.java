package com.business.trycoreevmtrackerapi.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trycore EVM Tracker API")
                        .version("1.0.0")
                        .description("API REST para la gestión de proyectos, actividades y cálculo en tiempo real de indicadores de Valor Ganado (Earned Value Management).")
                        .contact(new Contact()
                                .name("David Alejandro Mateus Martinez")
                                .email("alejomatheus98@outlook.com")));
    }
}
