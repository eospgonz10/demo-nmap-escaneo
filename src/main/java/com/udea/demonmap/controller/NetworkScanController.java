package com.udea.demonmap.controller;

import com.udea.demonmap.dto.NetworkDeviceDTO;
import com.udea.demonmap.dto.PortDTO;
import com.udea.demonmap.dto.ScanRequestDTO;
import com.udea.demonmap.dto.ScanResultDTO;
import com.udea.demonmap.entity.NetworkDevice;
import com.udea.demonmap.entity.Port;
import com.udea.demonmap.entity.ScanResult;
import com.udea.demonmap.repository.ScanException;
import com.udea.demonmap.service.NetworkScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones de escaneo de red.
 * 
 * Expone endpoints documentados con Swagger para:
 * - Escaneo completo de red con detección de dispositivos y puertos
 * - Escaneo rápido (solo dispositivos activos)
 * - Escaneo de host individual
 * - Detección automática de red local
 * 
 * Aplica principios SOLID:
 * - SRP: Única responsabilidad - manejar peticiones HTTP de escaneo
 * - DIP: Depende de abstracción (NetworkScanService interface)
 */
@Slf4j
@RestController
@RequestMapping("/api/network")
@RequiredArgsConstructor
@Validated
@Tag(name = "Network Scanner", description = "API para escaneo de red con nmap")
public class NetworkScanController {
    
    private final NetworkScanService networkScanService;
    
    @Operation(
        summary = "Escanear red completa",
        description = "Realiza un escaneo de la red detectando dispositivos activos y sus puertos abiertos. " +
                     "Usa concurrencia para mejorar el rendimiento del escaneo."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Escaneo completado exitosamente",
            content = @Content(schema = @Schema(implementation = ScanResultDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros inválidos"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno durante el escaneo"
        )
    })
    @GetMapping("/scan")
    public ResponseEntity<ScanResultDTO> scanNetwork(
            @Parameter(description = "Rango de red en notación CIDR (ej: 192.168.1.0/24). Si no se proporciona, se detecta automáticamente.", example = "192.168.1.0/24")
            @RequestParam(required = false) 
            String networkRange,
            
            @Parameter(description = "Tipo de escaneo: 'quick' (solo dispositivos activos) o 'full' (con puertos)", example = "full")
            @RequestParam(defaultValue = "quick")
            String scanType
    ) {
        try {
            // Validar tipo de escaneo
            if (!"quick".equalsIgnoreCase(scanType) && !"full".equalsIgnoreCase(scanType)) {
                throw new IllegalArgumentException("El tipo debe ser 'quick' o 'full'");
            }
            
            // Detectar red si no se proporciona
            if (networkRange == null || networkRange.isEmpty()) {
                networkRange = networkScanService.detectLocalNetwork();
                log.info("Red detectada automáticamente: {}", networkRange);
            }
            
            log.info("Iniciando escaneo tipo '{}' de la red: {}", scanType, networkRange);
            
            ScanResult result;
            if ("full".equalsIgnoreCase(scanType)) {
                result = networkScanService.performFullNetworkScan(networkRange);
            } else {
                result = networkScanService.performQuickScan(networkRange);
            }
            
            ScanResultDTO dto = toDTO(result);
            return ResponseEntity.ok(dto);
            
        } catch (ScanException e) {
            log.error("Error durante el escaneo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ScanResultDTO.builder()
                            .status("FAILED")
                            .errorMessage(e.getMessage())
                            .networkRange(networkRange)
                            .build());
        }
    }
    
    @Operation(
        summary = "Escanear host específico",
        description = "Realiza un escaneo detallado de un host específico, incluyendo detección de puertos y servicios."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Host escaneado exitosamente",
            content = @Content(schema = @Schema(implementation = NetworkDeviceDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dirección IP inválida"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error durante el escaneo del host"
        )
    })
    @GetMapping("/scan/host/{ipAddress}")
    public ResponseEntity<NetworkDeviceDTO> scanHost(
            @Parameter(description = "Dirección IP del host a escanear", example = "192.168.1.1")
            @PathVariable 
            @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
                    message = "Formato de IP inválido")
            String ipAddress
    ) {
        try {
            log.info("Escaneando host: {}", ipAddress);
            NetworkDevice device = networkScanService.scanSingleHost(ipAddress);
            NetworkDeviceDTO dto = toDeviceDTO(device);
            return ResponseEntity.ok(dto);
            
        } catch (ScanException e) {
            log.error("Error escaneando host {}: {}", ipAddress, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(NetworkDeviceDTO.builder()
                            .ipAddress(ipAddress)
                            .status("error")
                            .build());
        }
    }
    
    @Operation(
        summary = "Detectar red local",
        description = "Detecta automáticamente el rango de red local del sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Red local detectada exitosamente"
        )
    })
    @GetMapping("/detect")
    public ResponseEntity<Map<String, String>> detectNetwork() {
        String networkRange = networkScanService.detectLocalNetwork();
        Map<String, String> response = new HashMap<>();
        response.put("networkRange", networkRange);
        response.put("message", "Red local detectada automáticamente");
        
        log.info("Red local detectada: {}", networkRange);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Health check",
        description = "Verifica que el servicio de escaneo está disponible y nmap está instalado."
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Network Scanner API");
        health.put("message", "Servicio de escaneo de red operativo");
        return ResponseEntity.ok(health);
    }
    
    // Métodos de conversión Entity -> DTO
    
    private ScanResultDTO toDTO(ScanResult result) {
        return ScanResultDTO.builder()
                .networkRange(result.getNetworkRange())
                .scanStartTime(result.getScanStartTime())
                .scanEndTime(result.getScanEndTime())
                .durationMs(result.getDurationMs())
                .totalHostsScanned(result.getTotalHostsScanned())
                .activeHostsFound(result.getActiveHostsFound())
                .status(result.getStatus() != null ? result.getStatus().name() : "UNKNOWN")
                .errorMessage(result.getErrorMessage())
                .devices(result.getDevices() != null ? 
                        result.getDevices().stream()
                                .map(this::toDeviceDTO)
                                .collect(Collectors.toList()) : List.of())
                .build();
    }
    
    private NetworkDeviceDTO toDeviceDTO(NetworkDevice device) {
        return NetworkDeviceDTO.builder()
                .ipAddress(device.getIpAddress())
                .macAddress(device.getMacAddress())
                .hostname(device.getHostname())
                .status(device.getStatus())
                .operatingSystem(device.getOperatingSystem())
                .vendor(device.getVendor())
                .responseTime(device.getResponseTime())
                .openPorts(device.getOpenPorts() != null ?
                        device.getOpenPorts().stream()
                                .map(this::toPortDTO)
                                .collect(Collectors.toList()) : List.of())
                .build();
    }
    
    private PortDTO toPortDTO(Port port) {
        return PortDTO.builder()
                .portNumber(port.getPortNumber())
                .protocol(port.getProtocol())
                .state(port.getState())
                .service(port.getService())
                .version(port.getVersion())
                .build();
    }
}
