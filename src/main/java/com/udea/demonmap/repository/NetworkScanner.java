package com.udea.demonmap.repository;

import com.udea.demonmap.entity.NetworkDevice;
import com.udea.demonmap.entity.ScanResult;

import java.util.List;

/**
 * Interfaz para el escaneo de red.
 */
public interface NetworkScanner {
    
    /**
     * Escanea un rango de red para detectar dispositivos activos.
     * 
     * @param networkRange Rango de red en notación CIDR (ej: 192.168.1.0/24)
     * @return ScanResult con los dispositivos encontrados
     * @throws ScanException si hay un error durante el escaneo
     */
    ScanResult scanNetwork(String networkRange) throws ScanException;
    
    /**
     * Escanea una IP específica con detección de puertos.
     * 
     * @param ipAddress Dirección IP a escanear
     * @return NetworkDevice con la información del dispositivo
     * @throws ScanException si hay un error durante el escaneo
     */
    NetworkDevice scanHost(String ipAddress) throws ScanException;
    
    /**
     * Verifica si una IP está activa en la red.
     * 
     * @param ipAddress Dirección IP a verificar
     * @return true si el host está activo, false en caso contrario
     */
    boolean isHostAlive(String ipAddress);
    
    /**
     * Obtiene la lista de hosts activos en un rango de red.
     * 
     * @param networkRange Rango de red en notación CIDR
     * @return Lista de direcciones IP activas
     * @throws ScanException si hay un error durante el escaneo
     */
    List<String> getActiveHosts(String networkRange) throws ScanException;
}
