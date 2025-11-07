package com.universidad.chat.servidor.repository;

import com.universidad.chat.servidor.model.Canal;
import com.universidad.chat.servidor.model.Mensaje;
import com.universidad.chat.servidor.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    
    /**
     * Obtiene todos los mensajes privados entre dos usuarios (en ambas direcciones)
     * Ordenados por timestamp ascendente (del más antiguo al más reciente)
     */
    @Query("SELECT m FROM Mensaje m WHERE " +
           "(m.emisor = :usuario1 AND m.receptorUsuario = :usuario2) OR " +
           "(m.emisor = :usuario2 AND m.receptorUsuario = :usuario1) " +
           "ORDER BY m.timestamp ASC")
    List<Mensaje> findMessagesBetweenUsers(@Param("usuario1") Usuario usuario1, 
                                           @Param("usuario2") Usuario usuario2);
    
    /**
     * Obtiene todos los mensajes de un canal específico
     * Ordenados por timestamp ascendente
     */
    @Query("SELECT m FROM Mensaje m WHERE m.receptorCanal = :canal ORDER BY m.timestamp ASC")
    List<Mensaje> findMessagesByCanal(@Param("canal") Canal canal);
    
    /**
     * Obtiene todos los canales donde el usuario ha participado (tiene mensajes)
     */
    @Query("SELECT DISTINCT m.receptorCanal FROM Mensaje m WHERE m.receptorCanal IN :canales")
    List<Canal> findCanalesWithMessages(@Param("canales") List<Canal> canales);
}