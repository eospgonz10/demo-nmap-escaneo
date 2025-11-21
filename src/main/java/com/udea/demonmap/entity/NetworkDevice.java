package com.udea.demonmap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un dispositivo de red detectado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkDevice {
    

    private String ipAddress;
    
    private String macAddress;
    
    private String hostname;    
   
    private String status;

    private String operatingSystem;

    private String vendor;

    @Builder.Default
    private List<Port> openPorts = new ArrayList<>();
    
    private Long responseTime;
    
    public void addPort(Port port) {
        if (this.openPorts == null) {
            this.openPorts = new ArrayList<>();
        }
        this.openPorts.add(port);
    }
}
