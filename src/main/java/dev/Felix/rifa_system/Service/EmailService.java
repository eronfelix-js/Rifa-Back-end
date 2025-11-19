package dev.Felix.rifa_system.Service;

import dev.Felix.rifa_system.Entity.Compra;
import dev.Felix.rifa_system.Entity.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void enviarEmailAprovado(Compra compra, Usuario comprador, List<Integer> numeros) {
        log.info("Enviando email de aprovação para o usuário: {}", comprador.getEmail());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(comprador.getEmail());
            helper.setSubject("✅ Compra Aprovada - " + compra.getRifa().getTitulo());
            helper.setText(gerarHtmlAprovacao(compra, comprador, numeros), true);

            mailSender.send(message);

            log.info("✅ Email de aprovação enviado com sucesso para: {}", comprador.getEmail());

        } catch (MessagingException e) {
            log.error("❌ Erro ao enviar email de aprovação: {}", e.getMessage(), e);
            // Não propagar erro - email é secundário
        }

    }
    @Async
    public void enviarNotificacaoRejeicao(Compra compra, Usuario comprador, String motivo) {
        log.info("📧 Enviando email de rejeição para: {}", comprador.getEmail());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(comprador.getEmail());
            helper.setSubject("❌ Compra Rejeitada - " + compra.getRifa().getTitulo());
            helper.setText(gerarHtmlRejeicao(compra, comprador, motivo), true);

            mailSender.send(message);

            log.info("✅ Email de rejeição enviado com sucesso para: {}", comprador.getEmail());

        } catch (MessagingException e) {
            log.error("❌ Erro ao enviar email de rejeição: {}", e.getMessage(), e);
        }
    }

    /**
     * Email de boas-vindas (opcional)
     */
    @Async
    public void enviarEmailBoasVindas(Usuario usuario) {
        log.info("📧 Enviando email de boas-vindas para: {}", usuario.getEmail());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(usuario.getEmail());
            helper.setSubject("🎉 Bem-vindo ao Sistema de Rifas!");
            helper.setText(gerarHtmlBoasVindas(usuario), true);

            mailSender.send(message);

            log.info("✅ Email de boas-vindas enviado");

        } catch (MessagingException e) {
            log.error("❌ Erro ao enviar email de boas-vindas: {}", e.getMessage());
        }
    }

    /**
     * Email de compra pendente (lembrete)
     */
    @Async
    public void enviarLembreteCompraPendente(Compra compra, Usuario comprador) {
        log.info("📧 Enviando lembrete de compra pendente para: {}", comprador.getEmail());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(comprador.getEmail());
            helper.setSubject("⏰ Compra Expirando em Breve - " + compra.getRifa().getTitulo());
            helper.setText(gerarHtmlLembrete(compra, comprador), true);

            mailSender.send(message);

            log.info("✅ Lembrete enviado");

        } catch (MessagingException e) {
            log.error("❌ Erro ao enviar lembrete: {}", e.getMessage());
        }
    }

    private String gerarHtmlAprovacao(Compra compra, Usuario comprador, List<Integer> numeros) {
        String numerosFormatados = numeros.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { 
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
                            background-color: #f3f4f6;
                            padding: 20px;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background: white;
                            border-radius: 12px;
                            overflow: hidden;
                            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #10b981 0%%, #059669 100%%);
                            color: white;
                            padding: 40px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            font-size: 28px;
                            margin-bottom: 10px;
                        }
                        .content {
                            padding: 40px 30px;
                            line-height: 1.6;
                            color: #374151;
                        }
                        .info-box {
                            background: #f9fafb;
                            border-left: 4px solid #10b981;
                            padding: 20px;
                            margin: 25px 0;
                            border-radius: 4px;
                        }
                        .info-box h3 {
                            color: #10b981;
                            margin-bottom: 15px;
                            font-size: 18px;
                        }
                        .info-box p {
                            margin: 8px 0;
                        }
                        .info-box strong {
                            color: #1f2937;
                        }
                        .numeros {
                            background: white;
                            border: 2px solid #10b981;
                            padding: 15px;
                            border-radius: 8px;
                            font-size: 18px;
                            font-weight: bold;
                            color: #10b981;
                            text-align: center;
                            margin: 20px 0;
                        }
                        .button {
                            display: inline-block;
                            background: #10b981;
                            color: white;
                            padding: 14px 32px;
                            text-decoration: none;
                            border-radius: 8px;
                            font-weight: 600;
                            margin: 20px 0;
                            transition: background 0.3s;
                        }
                        .button:hover {
                            background: #059669;
                        }
                        .footer {
                            background: #f9fafb;
                            padding: 20px;
                            text-align: center;
                            color: #6b7280;
                            font-size: 14px;
                        }
                        .success-icon {
                            font-size: 48px;
                            margin-bottom: 10px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="success-icon">✅</div>
                            <h1>Compra Aprovada!</h1>
                            <p>Seus números foram confirmados</p>
                        </div>
                        
                        <div class="content">
                            <p>Olá <strong>%s</strong>,</p>
                            <p>Excelentes notícias! O vendedor aprovou seu pagamento e seus números já estão oficialmente registrados no sorteio.</p>
                            
                            <div class="info-box">
                                <h3>📋 Detalhes da Compra</h3>
                                <p><strong>Rifa:</strong> %s</p>
                                <p><strong>Quantidade de Números:</strong> %d</p>
                                <p><strong>Valor Pago:</strong> R$ %.2f</p>
                                <p><strong>Data de Aprovação:</strong> %s</p>
                            </div>
                            
                            <div class="numeros">
                                🎲 Seus Números: %s
                            </div>
                            
                            <p>Agora é só aguardar o sorteio! Desejamos boa sorte a você! 🍀</p>
                            
                            <center>
                                <a href="https://seu-site.com/minhas-compras/%s" class="button">
                                    Ver Minha Compra
                                </a>
                            </center>
                        </div>
                        
                        <div class="footer">
                            <p>Sistema de Rifas - Todos os direitos reservados</p>
                            <p>Este é um email automático, não responda.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                comprador.getNome(),
                compra.getRifa().getTitulo(),
                compra.getQuantidadeNumeros(),
                compra.getValorTotal(),
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                numerosFormatados,
                compra.getId()
        );
    }

    private String gerarHtmlRejeicao(Compra compra, Usuario comprador, String motivo) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { 
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
                            background-color: #f3f4f6;
                            padding: 20px;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background: white;
                            border-radius: 12px;
                            overflow: hidden;
                            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #ef4444 0%%, #dc2626 100%%);
                            color: white;
                            padding: 40px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            font-size: 28px;
                            margin-bottom: 10px;
                        }
                        .content {
                            padding: 40px 30px;
                            line-height: 1.6;
                            color: #374151;
                        }
                        .alert-box {
                            background: #fef2f2;
                            border-left: 4px solid #ef4444;
                            padding: 20px;
                            margin: 25px 0;
                            border-radius: 4px;
                        }
                        .alert-box h3 {
                            color: #ef4444;
                            margin-bottom: 15px;
                            font-size: 18px;
                        }
                        .button {
                            display: inline-block;
                            background: #3b82f6;
                            color: white;
                            padding: 14px 32px;
                            text-decoration: none;
                            border-radius: 8px;
                            font-weight: 600;
                            margin: 20px 0;
                        }
                        .footer {
                            background: #f9fafb;
                            padding: 20px;
                            text-align: center;
                            color: #6b7280;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div style="font-size: 48px; margin-bottom: 10px;">❌</div>
                            <h1>Compra Não Aprovada</h1>
                            <p>Ação necessária</p>
                        </div>
                        
                        <div class="content">
                            <p>Olá <strong>%s</strong>,</p>
                            <p>Infelizmente o vendedor não aprovou seu comprovante de pagamento.</p>
                            
                            <div class="alert-box">
                                <h3>📝 Motivo da Rejeição</h3>
                                <p>%s</p>
                            </div>
                            
                            <p><strong>O que fazer agora?</strong></p>
                            <ul style="margin: 15px 0; padding-left: 20px;">
                                <li>Verifique se o comprovante está legível</li>
                                <li>Confirme se o valor está correto</li>
                                <li>Envie um novo comprovante</li>
                            </ul>
                            
                            <p>Seus números foram liberados e estão disponíveis novamente.</p>
                            
                            <center>
                                <a href="https://seu-site.com/compras/%s" class="button">
                                    Enviar Novo Comprovante
                                </a>
                            </center>
                        </div>
                        
                        <div class="footer">
                            <p>Sistema de Rifas - Todos os direitos reservados</p>
                            <p>Este é um email automático, não responda.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                comprador.getNome(),
                motivo,
                compra.getId()
        );
    }

    private String gerarHtmlBoasVindas(Usuario usuario) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background: #f3f4f6; padding: 20px; }
                        .container { max-width: 600px; margin: 0 auto; background: white; 
                                    border-radius: 12px; overflow: hidden; }
                        .header { background: #3b82f6; color: white; padding: 40px 20px; text-align: center; }
                        .content { padding: 40px 30px; line-height: 1.6; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Bem-vindo!</h1>
                        </div>
                        <div class="content">
                            <p>Olá <strong>%s</strong>,</p>
                            <p>Sua conta foi criada com sucesso! Agora você pode participar de rifas incríveis.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(usuario.getNome());
    }

    private String gerarHtmlLembrete(Compra compra, Usuario comprador) {
        long minutosRestantes = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                compra.getDataExpiracao()
        ).toMinutes();

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>⏰ Sua compra está expirando em breve!</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Sua reserva de números expira em <strong>%d minutos</strong>.</p>
                    <p>Envie o comprovante de pagamento para garantir seus números!</p>
                </body>
                </html>
                """.formatted(comprador.getNome(), minutosRestantes);
    }
}
