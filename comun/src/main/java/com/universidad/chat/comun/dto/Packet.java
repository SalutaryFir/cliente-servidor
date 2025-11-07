package com.universidad.chat.comun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

@Data // Lombok para getters, setters, etc.
@AllArgsConstructor // Lombok para un constructor con todos los argumentos.
public class Packet implements Serializable {
    private ActionType action;
    private Object payload; // Los datos que enviamos (ej: un UserDTO)
}