package com.universidad.chat.servidor.model;

import jakarta.persistence.*;
import lombok.Data; // <-- Make sure this import is present
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "canales")
@Data // <-- This annotation is the key to solving the error
public class Canal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombreCanal;

    @ManyToOne
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "miembros_canal",
            joinColumns = @JoinColumn(name = "canal_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> miembros = new HashSet<>();
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}