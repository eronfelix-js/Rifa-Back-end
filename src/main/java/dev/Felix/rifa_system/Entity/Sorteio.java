package dev.Felix.rifa_system.Entity;

import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Entity.Usuario;
import dev.Felix.rifa_system.Enum.MetodoSorteio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sorteios", indexes = {
        @Index(name = "idx_rifa", columnList = "rifa_id"),
        @Index(name = "idx_data_sorteio", columnList = "data_sorteio")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sorteio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rifa_id", nullable = false, unique = true)
    private UUID rifaId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rifa_id", insertable = false, updatable = false)
    private Rifa rifa;

    @Column(nullable = false)
    private Integer numeroSorteado;

    @Column(name = "comprador_vencedor_id", nullable = false)
    private UUID compradorVencedorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_vencedor_id", insertable = false, updatable = false)
    private Usuario compradorVencedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoSorteio metodo;

    @Column(nullable = false, length = 64)
    private String hashVerificacao;

    @Column(nullable = false)
    private LocalDateTime dataSorteio;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    public boolean isAutomatico() {
        return this.metodo == MetodoSorteio.AUTOMATICO;
    }

    public boolean isManual() {
        return this.metodo == MetodoSorteio.MANUAL;
    }
}