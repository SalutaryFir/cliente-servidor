package com.universidad.chat.comun.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * DTO para sincronizar lista de usuarios disponibles entre servidores
 */
@Data
public class ServerUserListDTO implements Serializable {
    private String serverIP;        // IP del servidor que envía la lista
    private String serverName;      // Nombre del servidor
    private List<String> usernames; // Lista de usuarios conectados en ese servidor
}
