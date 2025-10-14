package dev.Felix.rifa_system.Entity;

import dev.Felix.rifa_system.Enum.StatusCompra;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compras", indexes = {
        @Index(name = "idx_rifa", columnList = "rifa_id"),
        @Index(name = "idx_comprador", columnList = "comprador_id"),
        @Index(name = "idx_status_expiracao", columnList = "status, data_expiracao")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rifa_id", nullable = false)
    private UUID rifaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rifa_id", insertable = false, updatable = false)
    private Rifa rifa;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id", insertable = false, updatable = false)
    private Usuario comprador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusCompra status = StatusCompra.PENDENTE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private Integer quantidadeNumeros;

    @Column(nullable = false)
    private LocalDateTime dataExpiracao;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    public boolean isPendente() {
        return this.status == StatusCompra.PENDENTE;
    }

    public boolean isPago() {
        return this.status == StatusCompra.PAGO;
    }

    public boolean isExpirado() {
        return this.status == StatusCompra.EXPIRADO;
    }

    public boolean estaExpirada() {
        return LocalDateTime.now().isAfter(this.dataExpiracao);
    }

    public void confirmarPagamento() {
        this.status = StatusCompra.PAGO;
    }

    public void expirar() {
        this.status = StatusCompra.EXPIRADO;
    }

    public void cancelar() {
        this.status = StatusCompra.CANCELADO;
    }
}
