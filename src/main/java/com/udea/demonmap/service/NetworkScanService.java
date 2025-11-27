package com.udea.demonmap.service;

import com.udea.demonmap.entity.NetworkDevice;
import com.udea.demonmap.entity.ScanResult;
import com.udea.demonmap.repository.ScanException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interfaz del servicio de escaneo de red.
 */
public interface NetworkScanService {
    
    /**
     * Realiza un escaneo completo de la red con detección de dispositivos y puertos.
     *
     * @param networkRange Rango de red  (ej: 192.168.1.0/24)
     * @return ScanResult con todos los dispositivos y sus puertos
     * @throws ScanException si hay un error durante el escaneo
     */
    ScanResult performFullNetworkScan(String networkRange) throws ScanException;
    
    /**
     * Realiza un escaneo rápido de la red (solo detecta dispositivos activos).
     * 
     * @param networkRange Rango de red
     * @return ScanResult con dispositivos activos sin detalle de puertos
     * @throws ScanException si hay un error durante el escaneo
     */
    ScanResult performQuickScan(String networkRange) throws ScanException;

    /**
     * Escanea un host específico.
     * 
     * @param ipAddress Dirección IP del host
     * @return NetworkDevice con información detallada
     * @throws ScanException si hay un error durante el escaneo
     */
    NetworkDevice scanSingleHost(String ipAddress) throws ScanException;
    
    /**
     * Detecta automáticamente el rango de red local.
     * 
     * @return String con el rango de red en notación CIDR
     */
    String detectLocalNetwork();
}
