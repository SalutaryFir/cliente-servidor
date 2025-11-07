package com.universidad.chat.servidor.repository;

import com.universidad.chat.servidor.model.Canal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CanalRepository extends JpaRepository<Canal, Long> {
    Optional<Canal> findByNombreCanal(String nombreCanal);
    List<Canal> findAllByMiembros_NombreUsuario(String nombreUsuario);
}