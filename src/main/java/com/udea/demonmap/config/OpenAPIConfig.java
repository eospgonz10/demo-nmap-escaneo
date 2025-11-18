package com.udea.demonmap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API.
 */
@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI networkScannerOpenAPI() {
        
        Contact contact = new Contact();
        contact.setName("Demo Nmap Project");
        contact.setEmail("demo@udea.edu.co");
        
        Info info = new Info()
                .title("Network Scanner API")
                .version("1.0.0")
                .description(
                    "API REST para escaneo de redes locales usando nmap. " +
                    "Permite detectar dispositivos activos, puertos abiertos, servicios y sistemas operativos. " +
                    "Implementa concurrencia para escaneos eficientes y sigue principios SOLID y Clean Code."
                )
                .contact(contact)
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
        
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Servidor Local");
        
        Server productionServer = new Server();
        productionServer.setUrl("/");
        productionServer.setDescription("Servidor Actual");
        
        return new OpenAPI()
                .info(info)
                .servers(List.of(productionServer, localServer));
    }
}
