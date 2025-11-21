package com.udea.demonmap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un puerto de red detectado en un dispositivo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Port {
    
    private Integer portNumber;

    private String protocol;

    private String state;
    
    private String service;

    private String version;
}
