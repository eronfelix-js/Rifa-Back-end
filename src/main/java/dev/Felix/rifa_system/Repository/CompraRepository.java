package dev.Felix.rifa_system.Repository;

import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Enum.StatusCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompraRepository extends JpaRepository<Compra, UUID> {

    /**
     * Busca compras de um usuário
     */
    Page<Compra> findByCompradorIdOrderByDataCriacaoDesc(UUID compradorId, Pageable pageable);

    /**
     * Busca compras de uma rifa
     */
    Page<Compra> findByRifaIdOrderByDataCriacaoDesc(UUID rifaId, Pageable pageable);

    /**
     * Busca compras pendentes expiradas (para limpar)
     */
    List<Compra> findByStatusAndDataExpiracaoBefore(StatusCompra status, LocalDateTime data);

    /**
     * Conta compras pagas de uma rifa
     */
    long countByRifaIdAndStatus(UUID rifaId, StatusCompra status);
}