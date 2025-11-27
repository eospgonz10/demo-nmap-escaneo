package com.udea.demonmap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Configuración del sistema de escaneo de red.
 * 
 * Permite ajustar parámetros de rendimiento:
 * - Tamaño del pool de threads
 * - Timeouts de escaneo
 * - Número de puertos a escanear
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "network.scan")
public class NetworkScanConfig {
    
    /**
     * Tamaño del pool de threads
     */
    @Min(value = 1, message = "Thread pool size debe ser al menos 1")
    @Max(value = 100, message = "Thread pool size no debe exceder 100")
    private int threadPoolSize = 20;

    @Min(value = 5, message = "Host timeout debe ser al menos 5 segundos")
    @Max(value = 300, message = "Host timeout no debe exceder 300 segundos")
    private int hostTimeoutSeconds = 30;
    
    /**
     * Número de puertos más comunes a escanear por host.
     * Valores recomendados:
     * - 10: Ultra rápido, solo puertos más críticos
     * - 20: Balanceado (default) - cubre servicios principales
     * - 50-100: Escaneo detallado, más lento
     */
    @Min(value = 1, message = "Top ports debe ser al menos 1")
    @Max(value = 1000, message = "Top ports no debe exceder 1000")
    private int topPorts = 20;
}
