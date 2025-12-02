package com.universidad.chat.comun.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO para lista de audios procesados/transcritos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudioListResponseDTO implements Serializable {
    private int totalAudios;
    private List<AudioInfoDTO> audios;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioInfoDTO implements Serializable {
        private Long id;
        private String sender;           // Usuario que envió el audio
        private String recipient;        // Usuario o canal destinatario
        private String audioFileName;    // Nombre del archivo
        private String transcription;    // Texto transcrito
        private String timestamp;        // Fecha/hora de envío
        private boolean isChannelMessage; // Si es mensaje de canal
    }
}
