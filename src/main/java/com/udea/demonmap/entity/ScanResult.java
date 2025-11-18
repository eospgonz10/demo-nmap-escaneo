package com.udea.demonmap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el resultado completo de un escaneo de red.
 * Encapsula todos los dispositivos encontrados y metadatos del escaneo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResult {
    
    /**
     * Rango de red escaneado (CIDR notation)
     */
    private String networkRange;
    
    /**
     * Fecha y hora de inicio del escaneo
     */
    private LocalDateTime scanStartTime;
    
    /**
     * Fecha y hora de finalización del escaneo
     */
    private LocalDateTime scanEndTime;
    
    /**
     * Duración total del escaneo en milisegundos
     */
    private Long durationMs;
    
    /**
     * Lista de dispositivos detectados
     */
    @Builder.Default
    private List<NetworkDevice> devices = new ArrayList<>();
    
    /**
     * Número total de hosts escaneados
     */
    private Integer totalHostsScanned;
    
    /**
     * Número de hosts activos encontrados
     */
    private Integer activeHostsFound;
    
    /**
     * Estado del escaneo (SUCCESS, PARTIAL, FAILED)
     */
    private ScanStatus status;
    
    /**
     * Mensaje de error si el escaneo falló
     */
    private String errorMessage;
    
    /**
     * Agrega un dispositivo a la lista de dispositivos encontrados
     * @param device El dispositivo a agregar
     */
    public void addDevice(NetworkDevice device) {
        if (this.devices == null) {
            this.devices = new ArrayList<>();
        }
        this.devices.add(device);
    }
    
    /**
     * Calcula la duración del escaneo
     */
    public void calculateDuration() {
        if (scanStartTime != null && scanEndTime != null) {
            this.durationMs = java.time.Duration.between(scanStartTime, scanEndTime).toMillis();
        }
    }
    
    /**
     * Estados posibles de un escaneo
     */
    public enum ScanStatus {
        SUCCESS,    // Escaneo completado exitosamente
        PARTIAL,    // Escaneo completado con algunos errores
        FAILED      // Escaneo falló completamente
    }
}
