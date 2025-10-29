package dev.Felix.rifa_system.Entity;

import dev.Felix.rifa_system.Enum.StatusRifa;
import dev.Felix.rifa_system.Enum.TipoRifa;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rifas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rifa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(length = 500)
    private String imagemUrl;

    @Column(nullable = false)
    private Integer quantidadeNumeros;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoPorNumero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusRifa status = StatusRifa.ATIVA;

    @Column(nullable = false)
    private LocalDateTime dataInicio;

    @Column
    private LocalDateTime dataLimite;

    @Column
    private LocalDateTime dataSorteio;

    @Column
    private Integer numeroVencedor;

    @Column(name = "comprador_vencedor_id")
    private UUID compradorVencedorId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sorteioAutomatico = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sortearAoVenderTudo = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoRifa tipo = TipoRifa.PAGA_MANUAL;

    public boolean isGratuita() {
        return this.tipo == TipoRifa.GRATUITA;
    }

    public boolean isPagaManual() {
        return this.tipo == TipoRifa.PAGA_MANUAL;
    }

    public boolean isPagaAutomatica() {
        return this.tipo == TipoRifa.PAGA_AUTOMATICA;
    }

    public boolean isAtiva() {
        return this.status == StatusRifa.ATIVA;
    }

    public boolean isSorteada() {
        return this.status == StatusRifa.SORTEADA;
    }

    public boolean podeSerCancelada() {
        return this.status == StatusRifa.ATIVA;
    }

    public BigDecimal calcularValorTotal() {
        return this.precoPorNumero.multiply(BigDecimal.valueOf(this.quantidadeNumeros));
    }
}