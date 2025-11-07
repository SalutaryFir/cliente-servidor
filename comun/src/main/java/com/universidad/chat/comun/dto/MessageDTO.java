package com.universidad.chat.comun.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Base64; // <-- Importar

@Data
public class MessageDTO implements Serializable {
    private String sender;
    private String recipient;
    private boolean isAudioMessage;
    private String content;
    private String audioFileName;
    private LocalDateTime timestamp; // Fecha/hora del mensaje

    // --- CAMBIOS ---
    // Quitamos el byte[]
    // private byte[] audioData;
    // Añadimos un String para Base64
    private String audioDataBase64;
}