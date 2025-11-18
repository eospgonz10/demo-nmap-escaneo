package com.udea.demonmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para representar un dispositivo de red.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkDeviceDTO {
    private String ipAddress;
    private String macAddress;
    private String hostname;
    private String status;
    private String operatingSystem;
    private String vendor;
    private List<PortDTO> openPorts;
    private Long responseTime;
}
