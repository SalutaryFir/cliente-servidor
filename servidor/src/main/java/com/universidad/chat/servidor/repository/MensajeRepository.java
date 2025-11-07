package com.universidad.chat.servidor.repository;

import com.universidad.chat.servidor.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    // Tampoco necesitamos métodos personalizados por ahora.
}