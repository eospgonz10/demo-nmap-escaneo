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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResult {
    
    private String networkRange;
    
    private LocalDateTime scanStartTime;

    private LocalDateTime scanEndTime;

    private Long durationMs;
    
    @Builder.Default
    private List<NetworkDevice> devices = new ArrayList<>();

    private Integer totalHostsScanned;

    private Integer activeHostsFound;
    
    private ScanStatus status;
    
    private String errorMessage;
    
    public void addDevice(NetworkDevice device) {
        if (this.devices == null) {
            this.devices = new ArrayList<>();
        }
        this.devices.add(device);
    }

    public void calculateDuration() {
        if (scanStartTime != null && scanEndTime != null) {
            this.durationMs = java.time.Duration.between(scanStartTime, scanEndTime).toMillis();
        }
    }

    public enum ScanStatus {
        SUCCESS,    // Escaneo completado exitosamente
        PARTIAL,    // Escaneo completado con algunos errores
        FAILED      // Escaneo falló completamente
    }
}
