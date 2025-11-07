package com.universidad.chat.servidor.service;

import com.universidad.chat.servidor.model.Canal;
import com.universidad.chat.servidor.model.Usuario;
import com.universidad.chat.servidor.repository.CanalRepository;
import com.universidad.chat.servidor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CanalService {

    @Autowired
    private CanalRepository canalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional // Asegura que toda la operación se complete o se revierta
    public Canal crearCanal(String nombreCanal, String creadorUsername) {
        // Buscamos al usuario creador
        Usuario creador = usuarioRepository.findByNombreUsuario(creadorUsername)
                .orElseThrow(() -> new IllegalStateException("Usuario creador no encontrado."));

        // Verificamos si ya existe un canal con ese nombre
        if (canalRepository.findByNombreCanal(nombreCanal).isPresent()) {
            throw new IllegalStateException("Ya existe un canal con ese nombre.");
        }

        // Creamos el nuevo canal
        Canal nuevoCanal = new Canal();
        nuevoCanal.setNombreCanal(nombreCanal);
        nuevoCanal.setCreador(creador);

        // Añadimos al creador como el primer miembro
        nuevoCanal.getMiembros().add(creador);

        return canalRepository.save(nuevoCanal);
    }

    @Transactional
    public void agregarMiembroACanal(String nombreCanal, String nuevoMiembroUsername) {
        // Buscamos el canal y el usuario
        Canal canal = canalRepository.findByNombreCanal(nombreCanal)
                .orElseThrow(() -> new IllegalStateException("El canal no existe."));
        Usuario nuevoMiembro = usuarioRepository.findByNombreUsuario(nuevoMiembroUsername)
                .orElseThrow(() -> new IllegalStateException("El usuario a añadir no existe."));

        // Verificamos si ya es miembro para no añadirlo dos veces
        if (canal.getMiembros().contains(nuevoMiembro)) {
            throw new IllegalStateException("El usuario ya es miembro de este canal.");
        }

        // Añadimos el nuevo miembro y guardamos los cambios
        canal.getMiembros().add(nuevoMiembro);
        canalRepository.save(canal);
    }

    public List<Canal> findCanalesPorMiembro(String username) {
        return canalRepository.findAllByMiembros_NombreUsuario(username);
    }
}