package dev.Felix.rifa_system.Repository;


import dev.Felix.rifa_system.Entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {

    /**
     * Busca pagamento pelo ID da compra
     */
    Optional<Pagamento> findByCompraId(UUID compraId);

    /**
     * Busca pagamento pelo reference ID (usado no webhook)
     */
    Optional<Pagamento> findByReferenceId(String referenceId);

    /**
     * Verifica se já existe pagamento para uma compra
     */
    boolean existsByCompraId(UUID compraId);
}
