package com.universidad.chat.servidor.service;

import com.universidad.chat.servidor.model.Usuario;
import com.universidad.chat.servidor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // Marca esta clase como un componente de Servicio para Spring.
public class UsuarioService {

    // Inyectamos el repositorio. El servicio no habla directo a la DB,
    // usa el repositorio para eso.
    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario registrarUsuario(Usuario nuevoUsuario) {
        // 1. Validar que el email no esté en uso.
        if (usuarioRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
            throw new IllegalStateException("El email ya está registrado.");
        }

        // 2. Validar que el nombre de usuario no esté en uso.
        if (usuarioRepository.findByNombreUsuario(nuevoUsuario.getNombreUsuario()).isPresent()) {
            throw new IllegalStateException("El nombre de usuario ya está en uso.");
        }

        // TODO: Encriptar la contraseña antes de guardar.
        // String contrasenaEncriptada = passwordEncoder.encode(nuevoUsuario.getPassword());
        // nuevoUsuario.setPassword(contrasenaEncriptada);

        // 3. Si todo está bien, guardamos el usuario.
        return usuarioRepository.save(nuevoUsuario);
    }

    public Usuario autenticarUsuario(String email, String password) {
        // 1. Buscamos al usuario por su email.
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("El email no está registrado."));

        // 2. Comparamos la contraseña.
        // TODO: ¡MUY IMPORTANTE! En un proyecto real, las contraseñas NUNCA se comparan así.
        // Se debe comparar el hash de la contraseña ingresada con el hash guardado en la DB.
        // Por ahora, para simplificar, lo hacemos en texto plano.
        if (!usuario.getPassword().equals(password)) {
            throw new IllegalStateException("Contraseña incorrecta.");
        }

        // 3. Si todo está bien, devolvemos el usuario.
        return usuario;
    }
}