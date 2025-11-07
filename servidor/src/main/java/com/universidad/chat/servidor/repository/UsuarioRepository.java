package com.universidad.chat.servidor.repository;

import com.universidad.chat.servidor.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Le indica a Spring que esta es una interfaz de repositorio.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // JpaRepository<TipoDeEntidad, TipoDeLaLlavePrimaria>

    // ¡Y eso es todo! Ya tenemos los métodos básicos para el CRUD de Usuarios.

    // Podemos añadir métodos personalizados. Spring los implementa por nosotros
    // basándose en el nombre del método.
    // Este método buscará un usuario por su columna "email".
    Optional<Usuario> findByEmail(String email);

    // Este otro buscará por el nombre de usuario.
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}