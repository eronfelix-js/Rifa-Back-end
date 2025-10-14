package dev.Felix.rifa_system.Repository;

import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Enum.StatusRifa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RifaRepository extends JpaRepository<Rifa, UUID> {

    boolean existsByUsuarioIdAndStatus(UUID usuarioId, StatusRifa status);
    Page<Rifa> findByStatus(StatusRifa status, Pageable pageable);
    List<Rifa> findByUsuarioIdOrderByDataCriacaoDesc(UUID usuarioId);
    Page<Rifa> findByStatusOrderByDataCriacaoDesc(StatusRifa status, Pageable pageable);
    @Query("SELECT r FROM Rifa r WHERE r.status = 'COMPLETA' AND r.sorteioAutomatico = true AND r.dataSorteio IS NULL")
    List<Rifa> findRifasParaSortear();
}