package com.business.trycoreevmtrackerapi.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.environment.url-local}")
    private String localUrl;

    @Value("${app.environment.url-prod}")
    private String prodUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trycore EVM Tracker API")
                        .description("API REST para la gestión de proyectos, actividades y cálculo en tiempo real de indicadores de Valor Ganado (Earned Value Management).")
                        .version("v0.1.0")
                        .contact(new Contact()
                                .name("David Alejandro Mateus Martinez")
                                .email("david.5.12@hotmail.com")))
                .servers(List.of(
                        new Server().url(localUrl).description("Entorno local"),
                        new Server().url(prodUrl).description("Producción")
                ));
    }
}
