package dev.Felix.rifa_system.Repository;


import dev.Felix.rifa_system.Entity.Sorteio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SorteioRepository extends JpaRepository<Sorteio, UUID> {

    Optional<Sorteio> findByRifaId(UUID rifaId);
    boolean existsByRifaId(UUID rifaId);
}