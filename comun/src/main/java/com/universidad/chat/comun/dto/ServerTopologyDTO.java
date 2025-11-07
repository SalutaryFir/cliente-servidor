package com.universidad.chat.comun.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * DTO para sincronizar la topología de servidores federados
 * Contiene la lista completa de servidores conocidos en la red
 */
@Data
public class ServerTopologyDTO implements Serializable {
    private List<ServerInfoDTO> servers; // Lista de todos los servidores en la red
}
