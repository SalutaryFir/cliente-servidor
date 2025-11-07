// Reemplaza el contenido de tu archivo con este
package com.universidad.chat.cliente.repository;

import com.universidad.chat.cliente.model.MensajeCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeCacheRepository extends JpaRepository<MensajeCache, Long> {

    // --- MÉTODO ACTUALIZADO ---
    // Ahora buscará los mensajes para un 'target' Y que pertenezcan a un 'owner'
    List<MensajeCache> findByChatTargetAndOwnerUsernameOrderByTimestampAsc(String chatTarget, String ownerUsername);
}