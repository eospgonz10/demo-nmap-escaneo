package com.udea.demonmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de escaneo de red.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanRequestDTO {
    
    /**
     * Rango de red (ej: 192.168.1.0/24)
     * Si no se proporciona, se detectará automáticamente la red local.
     */
    @Pattern(
        regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/(3[0-2]|[12]?[0-9])$",
        message = "El formato de red debe ser CIDR válido (ej: 192.168.1.0/24)"
    )
    private String networkRange;
    
    /**
     * Tipo de escaneo: "quick" (solo dispositivos activos) o "full" (con puertos)
     */
    @NotBlank(message = "El tipo de escaneo es obligatorio")
    @Pattern(
        regexp = "^(quick|full)$",
        message = "El tipo de escaneo debe ser 'quick' o 'full'"
    )
    private String scanType = "quick";
}
