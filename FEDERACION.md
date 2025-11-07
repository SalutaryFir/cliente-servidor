# 🌐 Sistema de Chat Federado - Guía de Uso

## Descripción General
El sistema ahora soporta **federación de servidores**: múltiples servidores independientes que se comunican entre sí, permitiendo que clientes de diferentes servidores intercambien mensajes, audios y participen en canales compartidos.

## Arquitectura

### Componentes Principales

1. **ServerRegistry**: Gestiona conexiones con otros servidores federados
2. **FederationListener**: Escucha conexiones entrantes de otros servidores en puerto S2S
3. **ClientHandler**: Enruta mensajes locales o hacia la federación
4. **DTOs de Federación**:
   - `ServerInfoDTO`: Información de un servidor (IP, puerto, nombre)
   - `FederatedMessageDTO`: Mensaje que viaja entre servidores
   - `ServerUserListDTO`: Sincronización de usuarios

### Flujo de Comunicación

```
Cliente A (Servidor 1) → Servidor 1 → [Red Federada] → Servidor 2 → Cliente B (Servidor 2)
```

## Cómo Usar la Federación

### 1. Iniciar Múltiples Servidores

#### Servidor Principal (Puerto TCP: 5000, Fed: 5001)
```cmd
cd c:\Users\ASUS\Desktop\chat-proyecto\servidor
mvn spring-boot:run
```

#### Servidor Secundario (Puerto TCP: 5002, Fed: 5003)
```cmd
cd c:\Users\ASUS\Desktop\chat-proyecto\servidor
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=server2
```

O alternativamente con JAR:
```cmd
java -jar servidor-0.0.1-SNAPSHOT.jar --chat.server.client-port=5004 --chat.server.name=Servidor-Terciario --chat.server.federation-port=5005
```

### 2. Conectar Servidores (Federarlos)

En la **UI del Servidor** (cualquiera de los dos):

1. En el panel **"Servidores Federados"** (parte inferior derecha)
2. Ingresar:
   - **IP**: `127.0.0.1` (o la IP real si están en máquinas diferentes)
   - **Puerto**: `5003` (el puerto de federación del otro servidor)
3. Click en **"Conectar"**

> **Nota**: Solo necesitas conectar desde UN servidor. La conexión es bidireccional.

### 3. Conectar Clientes

#### Cliente conectado al Servidor 1:
```cmd
cd c:\Users\ASUS\Desktop\chat-proyecto\cliente
mvn spring-boot:run
```
(Por defecto se conecta a `localhost:5000`)

#### Cliente conectado al Servidor 2:
```cmd
cd c:\Users\ASUS\Desktop\chat-proyecto\cliente
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=server2
```

Esto conectará el cliente al puerto 5002 (Servidor Secundario).

### 4. Probar Mensajería Federada

1. **Registra usuarios** en ambos servidores:
   - Servidor 1: usuarios `alice`, `bob`
   - Servidor 2: usuarios `charlie`, `diana`

2. **Login** con los usuarios en sus respectivos clientes

3. **Enviar mensajes privados**:
   - `alice` (Server 1) puede escribir a `charlie` (Server 2)
   - El mensaje viajará: Cliente Alice → Server 1 → [Fed] → Server 2 → Cliente Charlie

4. **Crear canales federados**:
   - `alice` crea canal `#general`
   - `alice` invita a `charlie` (que está en Server 2)
   - `charlie` acepta
   - Ahora ambos pueden chatear en el canal, aunque estén en servidores diferentes

5. **Enviar audios**:
   - Funciona igual que mensajes de texto
   - El archivo se guarda en el servidor origen
   - La transcripción viaja con la notificación

## UI del Servidor

### Panel de Federación (Nuevo)
Muestra tabla con:
- **Servidor**: Nombre del servidor federado
- **IP**: Dirección IP
- **Puerto**: Puerto de federación (S2S)
- **Clientes**: Número de clientes conectados (sincronizado)

### Logs
Busca líneas como:
- `✅ Servidor federado registrado: Servidor-Secundario (127.0.0.1:5002)`
- `📡 Mensaje reenviado a federación: charlie`
- `📨 Mensaje federado entregado a charlie`

## UI del Cliente

### Título de Ventana (Actualizado)
Ahora muestra:
```
Chat Universitario - alice @ Servidor-Principal (192.168.1.100)
```

### Lista de Usuarios
- Usuarios del **mismo servidor**: se muestran normalmente
- Usuarios de **otros servidores**: funcionan igual (futura mejora: añadir ícono 🌐)

## Puertos Utilizados

| Componente | Puerto por Defecto | Configurable en |
|------------|-------------------|-----------------|
| Servidor 1 - Clientes (TCP) | 5000 | `chat.server.client-port` |
| Servidor 1 - Federación (S2S) | 5001 | `chat.server.federation-port` |
| Servidor 2 - Clientes (TCP) | 5002 | `chat.server.client-port` |
| Servidor 2 - Federación (S2S) | 5003 | `chat.server.federation-port` |
| Spring Boot Admin (opcional) | 8080/8081 | `server.port` |

## IP local: usar 127.0.0.1 vs IP de la LAN

Cuando ejecutas todos los servidores en la misma máquina, es recomendable que todos usen `127.0.0.1` para evitar que la federación registre el mismo host con dos claves distintas (por ejemplo `127.0.0.1:5003` y `192.168.1.12:5003`).

Para forzar que el servidor se identifique como `127.0.0.1`, habilita esta propiedad:

```properties
# en application.properties o en el perfil que uses (application-server2.properties, etc.)
chat.server.use-localhost=true
```

Si piensas federar entre máquinas diferentes en la red local, déjalo en `false` (valor por defecto) para que use la IP de la LAN.

## Limitaciones Actuales

1. **Discovery Manual**: Debes conectar servidores manualmente desde la UI
2. **Caché de Usuarios**: No se sincroniza automáticamente la lista completa de usuarios remotos
3. **Routing Simple**: Los mensajes se envían por broadcast a todos los servidores (no hay routing inteligente)
4. **Audio Remoto**: Los archivos de audio se guardan solo en el servidor origen; el destino recibe la transcripción pero debe pedir el archivo al servidor origen para reproducirlo

## Mejoras Futuras

- [ ] Auto-discovery de servidores en la LAN (multicast)
- [ ] Sincronización de usuarios en tiempo real
- [ ] Routing inteligente (conocer en qué servidor está cada usuario)
- [ ] Replicación de archivos de audio entre servidores
- [ ] UI para mostrar origen del mensaje (badge del servidor)
- [ ] Heartbeat automático para detectar servidores caídos

## Troubleshooting

### "No se pudo conectar"
- Verifica que el servidor destino esté corriendo
- Verifica que el puerto de federación sea el correcto
- Verifica firewall (permite TCP en puertos 5001, 5002, etc.)

### "Destinatario no encontrado"
- El usuario podría no estar conectado
- Espera unos segundos; la sincronización puede tomar tiempo

### Mensajes no llegan a canales federados
- Verifica que ambos servidores estén federados (conectados)
- Verifica que el miembro del otro servidor haya aceptado la invitación
- Revisa logs del servidor: debe aparecer "📡 Mensaje de canal reenviado a federación"

## Testing Rápido

Script de PowerShell para arrancar 2 servidores y 2 clientes:

```powershell
# Terminal 1: Servidor Principal
cd C:\Users\ASUS\Desktop\chat-proyecto\servidor
mvn spring-boot:run

# Terminal 2: Servidor Secundario  
cd C:\Users\ASUS\Desktop\chat-proyecto\servidor
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=server2

# Terminal 3: Cliente 1 (conecta a Servidor Principal)
cd C:\Users\ASUS\Desktop\chat-proyecto\cliente
mvn spring-boot:run

# Terminal 4: Cliente 2 (conecta a Servidor Secundario)
cd C:\Users\ASUS\Desktop\chat-proyecto\cliente  
mvn spring-boot:run -Dspring-boot.run.arguments=--chat.client.server-port=8081
```

Luego:
1. En UI de Servidor 1: conectar a `127.0.0.1:5002`
2. Registrar usuarios en cada servidor
3. Login con los clientes
4. ¡Chatear entre servidores!

---
**Última actualización**: Noviembre 2025
**Versión**: 1.0 - Federación Básica
