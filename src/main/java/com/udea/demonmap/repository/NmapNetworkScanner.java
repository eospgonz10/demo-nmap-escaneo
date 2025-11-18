package com.udea.demonmap.repository;

import com.udea.demonmap.entity.NetworkDevice;
import com.udea.demonmap.entity.Port;
import com.udea.demonmap.entity.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementación del escáner de red usando nmap.
 * Aplica el principio SRP (Single Responsibility Principle):
 * - Única responsabilidad: ejecutar comandos nmap y parsear resultados.
 */
@Slf4j
@Repository
public class NmapNetworkScanner implements NetworkScanner {
    
    private static final String NMAP_COMMAND = "nmap";
    private static final int COMMAND_TIMEOUT_SECONDS = 300; // 5 minutos
    
    // Patrones regex para parsear salida de nmap
    private static final Pattern IP_PATTERN = Pattern.compile("Nmap scan report for ([\\w.-]+)? ?\\(?([0-9.]+)\\)?");
    private static final Pattern MAC_PATTERN = Pattern.compile("MAC Address: ([0-9A-F:]+) \\(([^)]+)\\)");
    private static final Pattern PORT_PATTERN = Pattern.compile("(\\d+)/(tcp|udp)\\s+(open|closed|filtered)\\s+([\\w-]+)(?:\\s+(.+))?");
    private static final Pattern OS_PATTERN = Pattern.compile("OS details: (.+)");
    private static final Pattern HOST_UP_PATTERN = Pattern.compile("Host is up");
    
    @Override
    public ScanResult scanNetwork(String networkRange) throws ScanException {
        log.info("Iniciando escaneo de red: {}", networkRange);
        
        LocalDateTime startTime = LocalDateTime.now();
        ScanResult result = ScanResult.builder()
                .networkRange(networkRange)
                .scanStartTime(startTime)
                .devices(new ArrayList<>())
                .build();
        
        try {
            // Comando nmap para escaneo rápido de red
            // -sn: Ping scan (no escanea puertos)
            // -PR: ARP ping
            String command = String.format("%s -sn -PR %s", NMAP_COMMAND, networkRange);
            
            log.debug("Ejecutando comando: {}", command);
            List<String> activeIps = executeCommand(command);
            
            // Parsear IPs activas
            List<String> ips = parseActiveHosts(activeIps);
            result.setTotalHostsScanned(ips.size());
            
            log.info("Hosts activos encontrados: {}", ips.size());
            
            result.setScanEndTime(LocalDateTime.now());
            result.calculateDuration();
            result.setActiveHostsFound(ips.size());
            result.setStatus(ScanResult.ScanStatus.SUCCESS);
            
            return result;
            
        } catch (Exception e) {
            log.error("Error durante el escaneo de red: {}", e.getMessage(), e);
            result.setScanEndTime(LocalDateTime.now());
            result.calculateDuration();
            result.setStatus(ScanResult.ScanStatus.FAILED);
            result.setErrorMessage(e.getMessage());
            throw new ScanException("Error al escanear la red: " + e.getMessage(), e);
        }
    }
    
    @Override
    public NetworkDevice scanHost(String ipAddress) throws ScanException {
        log.info("Escaneando host: {}", ipAddress);
        
        try {
            // Comando nmap para escaneo detallado de un host
            // -sV: Detección de versión de servicios
            // -O: Detección de OS
            // --top-ports 100: Escanea los 100 puertos más comunes
            String command = String.format("%s -sV --top-ports 100 %s", NMAP_COMMAND, ipAddress);
            
            log.debug("Ejecutando comando: {}", command);
            List<String> output = executeCommand(command);
            
            // Parsear resultado
            NetworkDevice device = parseHostScan(output, ipAddress);
            
            log.info("Host {} escaneado. Puertos abiertos: {}", ipAddress, device.getOpenPorts().size());
            
            return device;
            
        } catch (Exception e) {
            log.error("Error al escanear host {}: {}", ipAddress, e.getMessage(), e);
            throw new ScanException("Error al escanear host " + ipAddress + ": " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isHostAlive(String ipAddress) {
        try {
            String command = String.format("%s -sn %s", NMAP_COMMAND, ipAddress);
            List<String> output = executeCommand(command);
            
            for (String line : output) {
                if (HOST_UP_PATTERN.matcher(line).find()) {
                    return true;
                }
            }
            return false;
            
        } catch (Exception e) {
            log.warn("Error verificando host {}: {}", ipAddress, e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<String> getActiveHosts(String networkRange) throws ScanException {
        try {
            String command = String.format("%s -sn %s", NMAP_COMMAND, networkRange);
            List<String> output = executeCommand(command);
            return parseActiveHosts(output);
            
        } catch (Exception e) {
            throw new ScanException("Error obteniendo hosts activos: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ejecuta un comando del sistema y retorna la salida.
     * Compatible con Windows, Linux y macOS.
     */
    private List<String> executeCommand(String command) throws Exception {
        List<String> output = new ArrayList<>();
        
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        // Detectar sistema operativo y configurar comando apropiado
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows: usar cmd.exe
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            // Linux/Mac: usar sh
            processBuilder.command("sh", "-c", command);
        }
        
        Process process = processBuilder.start();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
                log.trace("nmap output: {}", line);
            }
        }
        
        // Leer errores si los hay
        try (BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                log.warn("nmap error: {}", errorLine);
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Comando nmap falló con código de salida: " + exitCode);
        }
        
        return output;
    }
    
    /**
     * Parsea la salida de nmap para extraer IPs activas.
     */
    private List<String> parseActiveHosts(List<String> output) {
        List<String> activeIps = new ArrayList<>();
        
        for (String line : output) {
            Matcher matcher = IP_PATTERN.matcher(line);
            if (matcher.find()) {
                String ip = matcher.group(2);
                if (ip != null && !ip.isEmpty()) {
                    activeIps.add(ip);
                }
            }
        }
        
        return activeIps;
    }
    
    /**
     * Parsea la salida detallada de nmap para un host específico.
     */
    private NetworkDevice parseHostScan(List<String> output, String ipAddress) {
        NetworkDevice.NetworkDeviceBuilder deviceBuilder = NetworkDevice.builder()
                .ipAddress(ipAddress)
                .status("unknown")
                .openPorts(new ArrayList<>());
        
        String currentHostname = null;
        String currentMac = null;
        String currentVendor = null;
        String currentOs = null;
        
        for (String line : output) {
            // Detectar hostname e IP
            Matcher ipMatcher = IP_PATTERN.matcher(line);
            if (ipMatcher.find()) {
                currentHostname = ipMatcher.group(1);
                if (line.contains("Host is up")) {
                    deviceBuilder.status("up");
                }
            }
            
            // Detectar MAC y vendor
            Matcher macMatcher = MAC_PATTERN.matcher(line);
            if (macMatcher.find()) {
                currentMac = macMatcher.group(1);
                currentVendor = macMatcher.group(2);
            }
            
            // Detectar OS
            Matcher osMatcher = OS_PATTERN.matcher(line);
            if (osMatcher.find()) {
                currentOs = osMatcher.group(1);
            }
            
            // Detectar puertos abiertos
            Matcher portMatcher = PORT_PATTERN.matcher(line);
            if (portMatcher.find()) {
                Port port = Port.builder()
                        .portNumber(Integer.parseInt(portMatcher.group(1)))
                        .protocol(portMatcher.group(2))
                        .state(portMatcher.group(3))
                        .service(portMatcher.group(4))
                        .version(portMatcher.group(5))
                        .build();
                
                deviceBuilder.openPorts(new ArrayList<>());
                // Necesitamos construir y agregar después
            }
            
            // Detectar estado del host
            if (HOST_UP_PATTERN.matcher(line).find()) {
                deviceBuilder.status("up");
            }
        }
        
        // Construir dispositivo y agregar puertos
        NetworkDevice device = deviceBuilder
                .hostname(currentHostname)
                .macAddress(currentMac)
                .vendor(currentVendor)
                .operatingSystem(currentOs)
                .build();
        
        // Agregar puertos parseados
        for (String line : output) {
            Matcher portMatcher = PORT_PATTERN.matcher(line);
            if (portMatcher.find() && "open".equals(portMatcher.group(3))) {
                Port port = Port.builder()
                        .portNumber(Integer.parseInt(portMatcher.group(1)))
                        .protocol(portMatcher.group(2))
                        .state(portMatcher.group(3))
                        .service(portMatcher.group(4))
                        .version(portMatcher.group(5))
                        .build();
                device.addPort(port);
            }
        }
        
        return device;
    }
}
