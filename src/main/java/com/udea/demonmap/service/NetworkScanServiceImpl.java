package com.udea.demonmap.service;

import com.udea.demonmap.config.NetworkScanConfig;
import com.udea.demonmap.entity.NetworkDevice;
import com.udea.demonmap.entity.ScanResult;
import com.udea.demonmap.repository.NetworkScanner;
import com.udea.demonmap.repository.ScanException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;

/**
 * Implementación del servicio de escaneo de red con concurrencia.
 *
 * Usa ExecutorService para procesamiento concurrente de múltiples hosts.
 */
@Slf4j
@Service
public class NetworkScanServiceImpl implements NetworkScanService {
    
    private final NetworkScanner networkScanner;
    private final NetworkScanConfig scanConfig;
    private final ExecutorService executorService;
    
    /**
     * Constructor con inyección de dependencias.
     * 
     * @param networkScanner Implementación del escáner (NmapNetworkScanner)
     * @param scanConfig Configuración desde application.properties
     */
    public NetworkScanServiceImpl(NetworkScanner networkScanner, NetworkScanConfig scanConfig) {
        this.networkScanner = networkScanner;
        this.scanConfig = scanConfig;

        // Pool threads - Propiedad: network.scan.thread-pool-size
        this.executorService = Executors.newFixedThreadPool(scanConfig.getThreadPoolSize());
        
        log.info("NetworkScanService inicializado con {} threads", scanConfig.getThreadPoolSize());
        log.info("Timeout por host: {} segundos", scanConfig.getHostTimeoutSeconds());
        log.info("Puertos a escanear: top {}", scanConfig.getTopPorts());
    }
    
    @Override
    public ScanResult performFullNetworkScan(String networkRange) throws ScanException {
        log.info("Iniciando escaneo completo de red: {}", networkRange);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // Paso 1: Detectar hosts activos
            log.debug("Paso 1: Detectando hosts activos...");
            List<String> activeHosts = networkScanner.getActiveHosts(networkRange);
            log.info("Hosts activos detectados: {}", activeHosts.size());
            
            if (activeHosts.isEmpty()) {
                log.warn("No se encontraron hosts activos en la red {}", networkRange);
                return ScanResult.builder()
                        .networkRange(networkRange)
                        .scanStartTime(startTime)
                        .scanEndTime(LocalDateTime.now())
                        .totalHostsScanned(0)
                        .activeHostsFound(0)
                        .devices(new ArrayList<>())
                        .status(ScanResult.ScanStatus.SUCCESS)
                        .build();
            }
            
            // Paso 2: Escanear cada host en paralelo para obtener puertos
            log.debug("Paso 2: Escaneando puertos de {} hosts en paralelo...", activeHosts.size());
            List<NetworkDevice> devices = scanHostsConcurrently(activeHosts);
            
            LocalDateTime endTime = LocalDateTime.now();
            
            ScanResult result = ScanResult.builder()
                    .networkRange(networkRange)
                    .scanStartTime(startTime)
                    .scanEndTime(endTime)
                    .totalHostsScanned(activeHosts.size())
                    .activeHostsFound(devices.size())
                    .devices(devices)
                    .status(ScanResult.ScanStatus.SUCCESS)
                    .build();
            
            result.calculateDuration();
            
            log.info("Escaneo completo finalizado. Duración: {} ms, Dispositivos: {}", 
                    result.getDurationMs(), devices.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error durante el escaneo completo: {}", e.getMessage(), e);
            
            ScanResult result = ScanResult.builder()
                    .networkRange(networkRange)
                    .scanStartTime(startTime)
                    .scanEndTime(LocalDateTime.now())
                    .status(ScanResult.ScanStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .devices(new ArrayList<>())
                    .build();
            
            result.calculateDuration();
            throw new ScanException("Error en escaneo completo: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ScanResult performQuickScan(String networkRange) throws ScanException {
        log.info("Iniciando escaneo rápido de red: {}", networkRange);
        return networkScanner.scanNetwork(networkRange);
    }
    
    @Override
    public NetworkDevice scanSingleHost(String ipAddress) throws ScanException {
        log.info("Escaneando host individual: {}", ipAddress);
        return networkScanner.scanHost(ipAddress);
    }
    
    @Override
    public String detectLocalNetwork() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // Ignorar interfaces inactivas o loopback
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    
                    // Solo IPv4
                    if (address.getAddress().length == 4) {
                        String ip = address.getHostAddress();
                        // Convertir a notación CIDR (asumiendo /24 para simplificar)
                        String networkPrefix = ip.substring(0, ip.lastIndexOf('.')) + ".0/24";
                        log.info("Red local detectada: {}", networkPrefix);
                        return networkPrefix;
                    }
                }
            }
            
            // Default si no se detecta
            log.warn("No se pudo detectar la red local, usando default: 192.168.1.0/24");
            return "192.168.1.0/24";
            
        } catch (Exception e) {
            log.error("Error detectando red local: {}", e.getMessage(), e);
            return "192.168.1.0/24";
        }
    }
    
    /**
     * Escanea múltiples hosts de forma concurrente usando ExecutorService.
     * Implementa el patrón de concurrencia para mejorar el rendimiento.
     * 
     * @param ipAddresses Lista de IPs a escanear
     * @return Lista de dispositivos escaneados
     */
    private List<NetworkDevice> scanHostsConcurrently(List<String> ipAddresses) {
        log.debug("Escaneando {} hosts concurrentemente con pool de {} threads", 
                ipAddresses.size(), scanConfig.getThreadPoolSize());
        
        List<Future<NetworkDevice>> futures = new ArrayList<>();
        
        // Crear tareas para cada host
        for (String ip : ipAddresses) {
            Future<NetworkDevice> future = executorService.submit(() -> {
                try {
                    log.trace("Escaneando host: {}", ip);
                    return networkScanner.scanHost(ip);
                } catch (ScanException e) {
                    log.warn("Error escaneando host {}: {}", ip, e.getMessage());
                    // Retornar dispositivo básico en caso de error
                    return NetworkDevice.builder()
                            .ipAddress(ip)
                            .status("error")
                            .openPorts(new ArrayList<>())
                            .build();
                }
            });
            futures.add(future);
        }
        
        // Recolectar resultados
        List<NetworkDevice> devices = new ArrayList<>();
        for (Future<NetworkDevice> future : futures) {
            try {
                // ⚡ TIMEOUT CONFIGURABLE desde application.properties
                // Propiedad: network.scan.host-timeout-seconds
                NetworkDevice device = future.get(scanConfig.getHostTimeoutSeconds(), TimeUnit.SECONDS);
                if (device != null && !"error".equals(device.getStatus())) {
                    devices.add(device);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("Error obteniendo resultado de escaneo: {}", e.getMessage());
            }
        }
        
        log.debug("Escaneo concurrente completado. Dispositivos válidos: {}", devices.size());
        return devices;
    }
}
