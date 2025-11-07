package com.universidad.chat.servidor.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
@Data
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emisor_id", nullable = false)
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "receptor_usuario_id")
    private Usuario receptorUsuario;

    @ManyToOne
    @JoinColumn(name = "receptor_canal_id")
    private Canal receptorCanal;

    @Lob
    private String contenidoTexto;

    private String rutaAudio;

    // Guardamos la fecha y hora de envío.
    @Column(nullable = false)
    private LocalDateTime timestamp;
}