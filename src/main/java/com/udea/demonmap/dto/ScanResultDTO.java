package com.udea.demonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para representar el resultado de un escaneo de red.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultDTO {
    private String networkRange;
    private LocalDateTime scanStartTime;
    private LocalDateTime scanEndTime;
    private Long durationMs;
    private List<NetworkDeviceDTO> devices;
    private Integer totalHostsScanned;
    private Integer activeHostsFound;
    private String status;
    private String errorMessage;
}
