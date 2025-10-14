package dev.Felix.rifa_system.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler para limpar reservas expiradas
 * Executa a cada 1 minuto
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaScheduler {

    private final CompraService compraService;

    /**
     * Limpa reservas expiradas a cada 1 minuto
     * fixedRate = 60000 ms = 1 minuto
     */
    @Scheduled(fixedRate = 60000)
    public void limparReservasExpiradas() {
        try {
            log.debug("Executando job de limpeza de reservas expiradas");

            int liberadas = compraService.limparReservasExpiradas();

            if (liberadas > 0) {
                log.info("✅ {} reservas expiradas foram liberadas", liberadas);
            }

        } catch (Exception e) {
            log.error("❌ Erro ao executar job de limpeza de reservas", e);
        }
    }

    /**
     * Log de status do scheduler (a cada 5 minutos)
     */
    @Scheduled(fixedRate = 300000)
    public void logStatus() {
        log.debug("📊 Scheduler de reservas está ativo e funcionando");
    }
}