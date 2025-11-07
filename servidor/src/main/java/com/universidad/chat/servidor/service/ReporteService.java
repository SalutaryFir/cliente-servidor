package com.universidad.chat.servidor.service;

import com.universidad.chat.servidor.model.Canal;
import com.universidad.chat.servidor.model.Mensaje;
import com.universidad.chat.servidor.model.Usuario;
import com.universidad.chat.servidor.network.TCPServer;
import com.universidad.chat.servidor.repository.CanalRepository;
import com.universidad.chat.servidor.repository.MensajeRepository;
import com.universidad.chat.servidor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CanalRepository canalRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private TCPServer tcpServer;

    // Formatter for consistent date/time in logs
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generates all reports and returns them combined in a single string.
     * @return A string containing all generated reports.
     */
    public String generarTodosLosReportesTexto() {
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("\n\n--- INICIO DE GENERACIÓN DE INFORMES ---\n");
        reportBuilder.append(generarReporteUsuariosRegistrados());
        reportBuilder.append(generarReporteUsuariosConectados());
        reportBuilder.append(generarReporteCanalesConMiembros());
        reportBuilder.append(generarReporteLogsDeMensajes());
        reportBuilder.append("\n--- FIN DE GENERACIÓN DE INFORMES ---\n");
        return reportBuilder.toString();
    }

    private String generarReporteUsuariosRegistrados() {
        StringBuilder sb = new StringBuilder("\n--- [Informe] Usuarios Registrados ---\n");
        List<Usuario> usuarios = usuarioRepository.findAll();
        if (usuarios.isEmpty()) {
            sb.append("No hay usuarios registrados.\n");
        } else {
            usuarios.forEach(u -> sb.append(" - ID: ").append(u.getId())
                    .append(", Usuario: ").append(u.getNombreUsuario())
                    .append(", Email: ").append(u.getEmail()).append("\n"));
        }
        return sb.toString();
    }

    private String generarReporteUsuariosConectados() {
        StringBuilder sb = new StringBuilder("\n--- [Informe] Usuarios Conectados Actualmente ---\n");
        List<String> usernames = tcpServer.getConnectedUsernames();
        if (usernames.isEmpty()) {
            sb.append("No hay usuarios conectados en este momento.\n");
        } else {
            usernames.forEach(name -> sb.append(" - ").append(name).append("\n"));
        }
        return sb.toString();
    }

    private String generarReporteCanalesConMiembros() {
        StringBuilder sb = new StringBuilder("\n--- [Informe] Canales y sus Miembros ---\n");
        List<Canal> canales = canalRepository.findAll();
        if (canales.isEmpty()) {
            sb.append("No hay canales creados.\n");
        } else {
            canales.forEach(canal -> {
                String miembros = canal.getMiembros().stream()
                        .map(Usuario::getNombreUsuario)
                        .collect(Collectors.joining(", "));
                sb.append(" - Canal: ").append(canal.getNombreCanal())
                        .append(" | Miembros: [").append(miembros).append("]\n");
            });
        }
        return sb.toString();
    }

    private String generarReporteLogsDeMensajes() {
        StringBuilder sb = new StringBuilder("\n--- [Informe] Log de Todos los Mensajes ---\n");
        List<Mensaje> mensajes = mensajeRepository.findAll();
        if (mensajes.isEmpty()) {
            sb.append("No se han enviado mensajes.\n");
        } else {
            mensajes.forEach(msg -> {
                String destino = "Desconocido";
                if (msg.getReceptorCanal() != null) {
                    destino = "Canal " + msg.getReceptorCanal().getNombreCanal();
                } else if (msg.getReceptorUsuario() != null) {
                    destino = "Usuario " + msg.getReceptorUsuario().getNombreUsuario();
                }
                String timestampFormateado = (msg.getTimestamp() != null) ? msg.getTimestamp().format(formatter) : "FECHA NULA";
                String textoMensaje = (msg.getContenidoTexto() != null) ? msg.getContenidoTexto() : "[Audio: " + msg.getRutaAudio() + "]";

                sb.append(" - [").append(timestampFormateado).append("] ")
                        .append("De: ").append(msg.getEmisor().getNombreUsuario()).append(" | ")
                        .append("Para: ").append(destino).append(" | ")
                        .append("Mensaje: '").append(textoMensaje).append("'\n");
            });
        }
        return sb.toString();
    }
}