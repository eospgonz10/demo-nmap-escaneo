# Network Scanner API - Demo Nmap

<div align="center">
  <img src="https://img.shields.io/badge/Java-17-blue?logo=java" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen?logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Nmap-Network%20Scanner-red?logo=linux" alt="Nmap"/>
  <img src="https://img.shields.io/badge/Swagger-API%20Docs-yellow?logo=swagger" alt="Swagger"/>
  <img src="https://img.shields.io/badge/SOLID-Principles-orange" alt="SOLID"/>
</div>

## 📋 Descripción

Aplicación Spring Boot que implementa un escáner de red concurrente usando **nmap** para detectar dispositivos activos, puertos abiertos, servicios y sistemas operativos en una red local. Proyecto demostrativo de aplicación de principios **SOLID**, **Clean Code** y **patrones de diseño**.

### 🎯 Características Principales

- ✅ **Escaneo de red completo** con detección de dispositivos y puertos
- ✅ **Procesamiento concurrente** usando ExecutorService para múltiples hosts
- ✅ **API RESTful** documentada con Swagger/OpenAPI
- ✅ **Arquitectura por capas** (Controller, Service, Repository, Entity, DTO)
- ✅ **Principios SOLID** aplicados en toda la arquitectura
- ✅ **Clean Code** con documentación clara y código autodescriptivo
- ✅ **Validación de datos** con Bean Validation
- ✅ **Manejo de errores** centralizado con GlobalExceptionHandler
- ✅ **Dev Container** configurado para Codespaces/Docker

---

## 🏗️ Arquitectura del Proyecto

```
src/main/java/com/udea/demonmap/
├── config/                    # Configuración (Swagger, etc.)
│   └── OpenAPIConfig.java
├── controller/                # Controladores REST
│   └── NetworkScanController.java
├── dto/                       # Data Transfer Objects
│   ├── NetworkDeviceDTO.java
│   ├── PortDTO.java
│   ├── ScanRequestDTO.java
│   └── ScanResultDTO.java
├── entity/                    # Entidades del dominio
│   ├── NetworkDevice.java
│   ├── Port.java
│   └── ScanResult.java
├── exception/                 # Manejo de excepciones
│   └── GlobalExceptionHandler.java
├── repository/                # Capa de acceso a datos (nmap)
│   ├── NetworkScanner.java         (Interface)
│   ├── NmapNetworkScanner.java     (Implementación)
│   └── ScanException.java
├── service/                   # Lógica de negocio
│   ├── NetworkScanService.java     (Interface)
│   └── NetworkScanServiceImpl.java (Implementación con concurrencia)
└── DemonmapApplication.java   # Aplicación principal
```

### 🎨 Principios SOLID Aplicados

#### **S - Single Responsibility Principle (SRP)**
- Cada clase tiene una única responsabilidad
- `NetworkDevice`: Representa un dispositivo de red
- `NmapNetworkScanner`: Ejecuta comandos nmap
- `NetworkScanServiceImpl`: Coordina escaneos con concurrencia
- `NetworkScanController`: Maneja peticiones HTTP

#### **O - Open/Closed Principle (OCP)**
- Clases abiertas a extensión, cerradas a modificación
- Se pueden agregar nuevos tipos de escaners sin modificar el servicio

#### **L - Liskov Substitution Principle (LSP)**
- Las implementaciones pueden sustituirse por sus abstracciones
- `NmapNetworkScanner` puede ser reemplazado por otra implementación de `NetworkScanner`

#### **I - Interface Segregation Principle (ISP)**
- Interfaces específicas y cohesivas
- `NetworkScanner` define solo operaciones de escaneo
- `NetworkScanService` define solo operaciones de alto nivel

#### **D - Dependency Inversion Principle (DIP)**
- Módulos de alto nivel dependen de abstracciones
- `NetworkScanServiceImpl` depende de `NetworkScanner` (interface)
- Inyección de dependencias mediante constructor

---

## 🚀 Requisitos Previos

### Para ejecución local:

- Java 17 o superior
- Maven 3.6+
- nmap instalado (`sudo apt install nmap` en Linux/Ubuntu)
- Git (opcional)

### Para ejecución en Codespaces:

- Cuenta de GitHub
- ✅ **Todo viene preconfigurado en el devcontainer**

---

## 📦 Instalación y Ejecución

### Opción 1: GitHub Codespaces (Recomendado)

1. Abre el repositorio en GitHub
2. Haz clic en **Code > Codespaces > Create codespace on main**
3. Espera que se construya el contenedor (incluye nmap preinstalado)
4. Una vez listo, el proyecto se compilará automáticamente
5. Ejecuta la aplicación:

```bash
./mvnw spring-boot:run
```

6. Accede a Swagger en la URL pública de tu Codespace:
   - Haz clic en el puerto 8080 que aparece en el panel de puertos
   - Agrega `/doc/swagger-ui.html` a la URL

### Opción 2: Ejecución Local

1. **Clonar el repositorio:**

```bash
git clone https://github.com/eospgonz10/demo-nmap-escaneo.git
cd demo-nmap-escaneo
```

2. **Instalar nmap:**

```bash
# En Ubuntu/Debian
sudo apt-get update
sudo apt-get install nmap

# En macOS
brew install nmap

# En Windows
# Descargar de https://nmap.org/download.html
```

3. **Compilar y ejecutar:**

```bash
./mvnw clean install
./mvnw spring-boot:run
```

4. **Acceder a la documentación Swagger:**

```
http://localhost:8080/doc/swagger-ui.html
```

---

## 🔧 Endpoints de la API

### 1. **Escanear Red**

**GET** `/api/network/scan`

Escanea una red completa detectando dispositivos y puertos.

**Parámetros:**
- `networkRange` (opcional): Rango de red en CIDR (ej: `192.168.1.0/24`)
  - Si no se proporciona, se detecta automáticamente
- `scanType` (opcional): Tipo de escaneo
  - `quick`: Solo detecta dispositivos activos (rápido)
  - `full`: Escanea dispositivos + puertos (más lento, más información)

**Ejemplo:**
```bash
curl "http://localhost:8080/api/network/scan?scanType=full"
```

**Respuesta:**
```json
{
  "networkRange": "192.168.1.0/24",
  "scanStartTime": "2025-11-18T10:30:00",
  "scanEndTime": "2025-11-18T10:32:15",
  "durationMs": 135000,
  "totalHostsScanned": 254,
  "activeHostsFound": 12,
  "status": "SUCCESS",
  "devices": [
    {
      "ipAddress": "192.168.1.1",
      "macAddress": "AA:BB:CC:DD:EE:FF",
      "hostname": "router.local",
      "status": "up",
      "vendor": "TP-Link",
      "openPorts": [
        {
          "portNumber": 80,
          "protocol": "tcp",
          "state": "open",
          "service": "http",
          "version": "nginx 1.18.0"
        },
        {
          "portNumber": 22,
          "protocol": "tcp",
          "state": "open",
          "service": "ssh"
        }
      ]
    }
  ]
}
```

### 2. **Escanear Host Específico**

**GET** `/api/network/scan/host/{ipAddress}`

Escanea un host específico en detalle.

**Ejemplo:**
```bash
curl "http://localhost:8080/api/network/scan/host/192.168.1.1"
```

### 3. **Detectar Red Local**

**GET** `/api/network/detect`

Detecta automáticamente el rango de red local.

**Ejemplo:**
```bash
curl "http://localhost:8080/api/network/detect"
```

### 4. **Health Check**

**GET** `/api/network/health`

Verifica que el servicio está operativo.

---

## 🧪 Pruebas con Swagger

1. Inicia la aplicación
2. Abre Swagger UI: `http://localhost:8080/doc/swagger-ui.html`
3. Explora los endpoints disponibles
4. Prueba el escaneo:
   - Usa `GET /api/network/detect` para obtener tu red local
   - Usa `GET /api/network/scan` con `scanType=quick` para un escaneo rápido
   - Usa `GET /api/network/scan` con `scanType=full` para escaneo completo

---

## ⚡ Concurrencia y Rendimiento

El servicio implementa **procesamiento concurrente** para mejorar el rendimiento:

- **ExecutorService** con pool de 10 threads
- Escaneo paralelo de múltiples hosts
- Timeout de 60 segundos por host
- Manejo robusto de errores en threads individuales

**Ejemplo de mejora:**
- Escaneo secuencial de 50 hosts: ~500 segundos
- Escaneo concurrente de 50 hosts: ~60 segundos
- **Mejora: 8x más rápido** ⚡

---

## 📚 Patrones de Diseño Aplicados

### **1. Dependency Injection (DI)**
- Uso de inyección por constructor con `@RequiredArgsConstructor`
- Facilita testing y desacoplamiento

### **2. Strategy Pattern**
- `NetworkScanner` como estrategia para diferentes tipos de escaneo
- Permite cambiar implementaciones sin modificar servicios

### **3. Builder Pattern**
- Construcción fluida de objetos complejos (Entity y DTO)
- Uso de `@Builder` de Lombok

### **4. DTO Pattern**
- Separación entre entidades de dominio y objetos de transferencia
- Control sobre lo que se expone en la API

### **5. Repository Pattern**
- `NetworkScanner` como repositorio de datos de red
- Abstracción del acceso a nmap

---

## 🛡️ Consideraciones de Seguridad

⚠️ **Importante**: Este proyecto es **solo para fines educativos y demostrativos**.

- ✅ Solo escanea redes locales
- ⚠️ Requiere permisos de administrador para algunos escaneos
- 🔒 No implementa autenticación (en producción, agregar JWT/OAuth2)
- 📝 Registra todas las operaciones para auditoría
- ⛔ No realizar escaneos en redes públicas o sin autorización

---

## 🐛 Troubleshooting

### Error: "nmap: command not found"
**Solución:** Instalar nmap según tu sistema operativo (ver sección de instalación)

### Error: "Permission denied"
**Solución:** Algunos escaneos requieren privilegios elevados:
```bash
sudo ./mvnw spring-boot:run
```

### Swagger no carga
**Solución:** Verifica que la aplicación esté corriendo y accede a:
```
http://localhost:8080/doc/swagger-ui.html
```

### En Codespaces: "Connection refused"
**Solución:** Usa la URL pública del puerto 8080 proporcionada por Codespaces

---

## 📖 Recursos Adicionales

- [Documentación de nmap](https://nmap.org/book/man.html)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Principios SOLID](https://en.wikipedia.org/wiki/SOLID)
- [Clean Code by Robert C. Martin](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)

---

## 👨‍💻 Autor

**Demo Nmap Project**  
Universidad de Antioquia  
Demostración de concurrencia, SOLID y Clean Code

---

## 📄 Licencia

Este proyecto es de código abierto bajo la licencia MIT.

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para cambios importantes:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

**¡Feliz escaneo! 🔍🌐**
Demo para implementar concurrencia escaneo ip red local
