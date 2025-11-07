package com.universidad.chat.comun.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserDTO implements Serializable {
    private String username;
    private String email;
    private String password; // Solo se usará para registro y login.
    private String serverIP; // IP del servidor donde está conectado el usuario
    private String serverName; // Nombre del servidor (para mostrar en UI)
}