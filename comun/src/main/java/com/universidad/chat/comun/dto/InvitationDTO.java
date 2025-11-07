package com.universidad.chat.comun.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class InvitationDTO implements Serializable {
    private String inviterUsername;  // Quién invita
    private String invitedUsername;  // A quién se invita
    private String channelName;      // A qué canal
    private boolean accepted;        // La respuesta (true si acepta, false si rechaza)
}