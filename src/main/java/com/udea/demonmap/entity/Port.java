package com.udea.demonmap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un puerto de red detectado en un dispositivo.
 * Esta entidad encapsula la información sobre puertos TCP/UDP abiertos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Port {
    
    /**
     * Número del puerto (1-65535)
     */
    private Integer portNumber;
    
    /**
     * Protocolo del puerto (TCP/UDP)
     */
    private String protocol;
    
    /**
     * Estado del puerto (open, closed, filtered)
     */
    private String state;
    
    /**
     * Servicio identificado en el puerto (http, ssh, etc.)
     */
    private String service;
    
    /**
     * Versión del servicio si está disponible
     */
    private String version;
}
