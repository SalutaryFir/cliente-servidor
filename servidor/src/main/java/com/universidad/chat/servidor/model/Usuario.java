package com.universidad.chat.servidor.model;

import jakarta.persistence.*;
import lombok.Data; // De la dependencia Lombok

@Entity // Le dice a Spring que esta clase es una tabla en la base de datos.
@Table(name = "usuarios") // Le damos un nombre específico a la tabla.
@Data // ¡Magia de Lombok! Crea automáticamente getters, setters, toString(), etc.
public class Usuario {

    @Id // Marca este campo como la llave primaria (PK).
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Le dice a MySQL que genere el ID automáticamente.
    private Long id;

    @Column(unique = true, nullable = false) // Columna única y que no puede ser nula.
    private String nombreUsuario;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String rutaFoto; // No necesita anotación @Column si se llama igual que en la DB.
}