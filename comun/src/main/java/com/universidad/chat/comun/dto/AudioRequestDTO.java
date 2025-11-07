package com.universidad.chat.comun.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class AudioRequestDTO implements Serializable {
    private String fileName; // El nombre único del archivo guardado en el servidor
}