package dev.Felix.rifa_system.Entity;

import dev.Felix.rifa_system.Enum.StatusNumero;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "numeros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Numero {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rifa_id", nullable = false)
    private UUID rifaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rifa_id", insertable = false, updatable = false)
    private Rifa rifa;

    @Column(nullable = false)
    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusNumero status = StatusNumero.DISPONIVEL;

    @Column(name = "compra_id")
    private UUID compraId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", insertable = false, updatable = false)
    private Compra compra;

    @Column
    private LocalDateTime dataReserva;

    @Column
    private LocalDateTime dataVenda;

    public boolean isDisponivel() {
        return this.status == StatusNumero.DISPONIVEL;
    }

    public boolean isReservado() {
        return this.status == StatusNumero.RESERVADO;
    }

    public boolean isVendido() {
        return this.status == StatusNumero.VENDIDO;
    }

    public void reservar(UUID compraId) {
        this.status = StatusNumero.RESERVADO;
        this.compraId = compraId;
        this.dataReserva = LocalDateTime.now();
    }

    public void vender() {
        this.status = StatusNumero.VENDIDO;
        this.dataVenda = LocalDateTime.now();
    }

    public void liberar() {
        this.status = StatusNumero.DISPONIVEL;
        this.compraId = null;
        this.dataReserva = null;
    }
}
