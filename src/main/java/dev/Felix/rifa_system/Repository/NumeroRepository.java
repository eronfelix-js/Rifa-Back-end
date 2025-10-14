package dev.Felix.rifa_system.Repository;


import dev.Felix.rifa_system.Entity.Numero;
import dev.Felix.rifa_system.Enum.StatusNumero;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NumeroRepository extends JpaRepository<Numero, UUID> {

    long countByRifaIdAndStatus(UUID rifaId, StatusNumero status);
    List<Numero> findByRifaIdAndStatus(UUID rifaId, StatusNumero status);

    /**Busca números disponíveis COM LOCK PESSIMISTA (evita race condition)*/
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM Numero n WHERE n.rifaId = :rifaId AND n.status = 'DISPONIVEL' ORDER BY n.numero")
    List<Numero> findDisponiveisComLock(@Param("rifaId") UUID rifaId, Pageable pageable);

    /**Busca números específicos disponíveis COM LOCK**/
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM Numero n WHERE n.rifaId = :rifaId AND n.numero IN :numeros AND n.status = 'DISPONIVEL'")
    List<Numero> findNumerosEspecificosComLock(@Param("rifaId") UUID rifaId, @Param("numeros") List<Integer> numeros);
    List<Numero> findByCompraId(UUID compraId);

}