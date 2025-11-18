package com.udea.demonmap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un dispositivo de red detectado.
 * Aplica el principio SRP (Single Responsibility Principle)
 * - Una única responsabilidad: representar un dispositivo de red.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkDevice {
    
    /**
     * Dirección IP del dispositivo
     */
    private String ipAddress;
    
    /**
     * Dirección MAC del dispositivo
     */
    private String macAddress;
    
    /**
     * Nombre del host si está disponible
     */
    private String hostname;
    
    /**
     * Estado del dispositivo (up/down)
     */
    private String status;
    
    /**
     * Sistema operativo detectado
     */
    private String operatingSystem;
    
    /**
     * Fabricante del dispositivo según la MAC
     */
    private String vendor;
    
    /**
     * Lista de puertos abiertos en el dispositivo
     */
    @Builder.Default
    private List<Port> openPorts = new ArrayList<>();
    
    /**
     * Tiempo de respuesta en milisegundos
     */
    private Long responseTime;
    
    /**
     * Agrega un puerto a la lista de puertos abiertos
     * @param port El puerto a agregar
     */
    public void addPort(Port port) {
        if (this.openPorts == null) {
            this.openPorts = new ArrayList<>();
        }
        this.openPorts.add(port);
    }
}
