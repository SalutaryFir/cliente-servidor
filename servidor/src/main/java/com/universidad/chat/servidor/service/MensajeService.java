package com.universidad.chat.servidor.service;

import com.universidad.chat.comun.dto.MessageDTO;
import com.universidad.chat.servidor.model.Canal;
import com.universidad.chat.servidor.model.Mensaje;
import com.universidad.chat.servidor.model.Usuario;
import com.universidad.chat.servidor.repository.CanalRepository;
import com.universidad.chat.servidor.repository.MensajeRepository;
import com.universidad.chat.servidor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64; // <-- Importar Base64

@Service
public class MensajeService {

    @Autowired
    private MensajeRepository mensajeRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CanalRepository canalRepository;
    @Autowired
    private TranscriptionService transcriptionService; // Necesario para la transcripción

    @Transactional
    public void guardarMensaje(MessageDTO messageDTO) {
        Usuario emisor = usuarioRepository.findByNombreUsuario(messageDTO.getSender())
                .orElseThrow(() -> new IllegalStateException("El emisor del mensaje no existe."));

        Mensaje nuevoMensaje = new Mensaje();
        nuevoMensaje.setEmisor(emisor);
        nuevoMensaje.setTimestamp(LocalDateTime.now());

        if (messageDTO.isAudioMessage()) {
            // Es un mensaje de audio
            // 1. Guardamos la RUTA/NOMBRE del archivo (que viene en audioFileName)
            nuevoMensaje.setRutaAudio(messageDTO.getAudioFileName());

            // 2. Obtenemos la TRANSCRIPCIÓN SIMULADA (que ya está en content)
            //    No necesitamos llamar a transcriptionService aquí, ClientHandler ya lo hizo.
            nuevoMensaje.setContenidoTexto(messageDTO.getContent());

        } else {
            // Es un mensaje de texto normal
            nuevoMensaje.setContenidoTexto(messageDTO.getContent());
            nuevoMensaje.setRutaAudio(null); // Aseguramos que la ruta de audio sea nula
        }

        // Lógica para asignar receptor (usuario o canal)
        if (messageDTO.getRecipient().startsWith("#")) {
            // Es un canal
            Canal canalReceptor = canalRepository.findByNombreCanal(messageDTO.getRecipient()).orElse(null);
            if (canalReceptor != null) {
                nuevoMensaje.setReceptorCanal(canalReceptor);
            } else {
                // El canal no existe localmente (es un canal remoto)
                System.out.println("⚠️ Canal remoto detectado, mensaje no guardado en BD local: " + messageDTO.getRecipient());
                return; // Salir sin guardar
            }
        } else {
            // Es un usuario
            Usuario usuarioReceptor = usuarioRepository.findByNombreUsuario(messageDTO.getRecipient()).orElse(null);
            if (usuarioReceptor != null) {
                nuevoMensaje.setReceptorUsuario(usuarioReceptor);
            } else {
                // El usuario no existe localmente (es usuario remoto)
                System.out.println("⚠️ Usuario remoto detectado, mensaje no guardado en BD local: " + messageDTO.getRecipient());
                return; // Salir sin guardar
            }
        }

        mensajeRepository.save(nuevoMensaje);
        System.out.println("Log de mensaje (con transcripción si es audio) guardado en DB.");
    }
}