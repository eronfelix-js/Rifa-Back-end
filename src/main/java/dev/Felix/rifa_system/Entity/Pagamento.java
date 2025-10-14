package dev.Felix.rifa_system.Entity;

import dev.Felix.rifa_system.Enum.StatusPagamento;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "compra_id", nullable = false, unique = true)
    private UUID compraId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", insertable = false, updatable = false)
    private Compra compra;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String gateway = "PICPAY";

    @Column(name = "reference_id", nullable = false, unique = true, length = 100)
    private String referenceId;

    @Column(name = "authorization_id", length = 100)
    private String authorizationId;

    @Column(columnDefinition = "TEXT")
    private String qrCode;

    @Column(columnDefinition = "TEXT")
    private String qrCodePayload;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusPagamento status = StatusPagamento.AGUARDANDO;

    @Column
    private LocalDateTime dataExpiracao;

    @Column
    private LocalDateTime dataPagamento;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    public boolean isAguardando() {
        return this.status == StatusPagamento.AGUARDANDO;
    }

    public boolean isAprovado() {
        return this.status == StatusPagamento.APROVADO;
    }

    public boolean isRecusado() {
        return this.status == StatusPagamento.RECUSADO;
    }

    public void aprovar(String authorizationId) {
        this.status = StatusPagamento.APROVADO;
        this.authorizationId = authorizationId;
        this.dataPagamento = LocalDateTime.now();
    }

    public void recusar() {
        this.status = StatusPagamento.RECUSADO;
    }

    public void expirar() {
        this.status = StatusPagamento.EXPIRADO;
    }
}
