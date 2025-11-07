// Reemplaza el contenido de tu archivo con este
package com.universidad.chat.cliente.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class MensajeCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatTarget;

    @Lob
    private String displayText;

    private String audioFileName;

    private LocalDateTime timestamp;


    private String ownerUsername;
}