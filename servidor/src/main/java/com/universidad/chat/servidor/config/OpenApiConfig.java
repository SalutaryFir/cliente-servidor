package com.universidad.chat.servidor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI para documentación automática de la API REST
 */
@Configuration
public class OpenApiConfig {

    @Value("${chat.server.name:Servidor-Principal}")
    private String serverName;

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription(serverName + " - Entorno de Desarrollo");

        Contact contact = new Contact();
        contact.setName("Equipo de Desarrollo Chat Federado");
        contact.setEmail("support@chatfederado.com");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Chat Federado - API de Administración del Servidor")
                .version("1.0.0")
                .description("API REST para monitoreo y administración del servidor de chat federado. " +
                        "Permite obtener información del servidor, usuarios, canales, logs y estadísticas. " +
                        "Diseñada para ser consumida por API Gateway y aplicaciones web de administración.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
