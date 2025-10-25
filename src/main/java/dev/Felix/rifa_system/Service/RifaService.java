package dev.Felix.rifa_system.Service;


import dev.Felix.rifa_system.Entity.Numero;
import dev.Felix.rifa_system.Entity.Rifa;
import dev.Felix.rifa_system.Enum.StatusNumero;
import dev.Felix.rifa_system.Enum.StatusRifa;
import dev.Felix.rifa_system.Exceptions.BusinessException;
import dev.Felix.rifa_system.Exceptions.ResourceNotFoundException;
import dev.Felix.rifa_system.Mapper.DtoImage.ImagemUploadResponse;
import dev.Felix.rifa_system.Mapper.DtoRifa.CriarRifaRequest;
import dev.Felix.rifa_system.Mapper.RifaMapper;
import dev.Felix.rifa_system.Repository.NumeroRepository;
import dev.Felix.rifa_system.Repository.RifaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RifaService {

    private final RifaRepository rifaRepository;
    private final NumeroRepository numeroRepository;
    private final UsuarioService usuarioService;
    private final RifaMapper rifaMapper;
    private final ImagemService imagemService;

    private Integer quantidadeMinima = 50;
    private Integer quantidadeMaxima = 100000;
    private Integer multiploDe = 10;

    @Transactional
    public Rifa criar(Rifa rifa) {
        log.info("Criando nova rifa: {} - Usuário: {}", rifa.getTitulo(), rifa.getUsuarioId());

        usuarioService.validarVendedor(rifa.getUsuarioId());

        if (rifaRepository.existsByUsuarioIdAndStatus(rifa.getUsuarioId(), StatusRifa.ATIVA)) {
            throw new BusinessException("Você já possui uma rifa ativa. Finalize-a antes de criar outra.");
        }

        validarQuantidadeNumeros(rifa.getQuantidadeNumeros());

        rifa.setStatus(StatusRifa.ATIVA);
        rifa.setDataInicio(LocalDateTime.now());

        if (rifa.getSorteioAutomatico() == null) {
            rifa.setSorteioAutomatico(true);
        }
        if (rifa.getSortearAoVenderTudo() == null) {
            rifa.setSortearAoVenderTudo(true);
        }

        Rifa rifaSalva = rifaRepository.save(rifa);
        gerarNumeros(rifaSalva);

        log.info("Rifa criada com sucesso: {}", rifaSalva.getId());
        return rifaSalva;
    }

    @Transactional
    public Rifa criarComImagem(CriarRifaRequest request, MultipartFile imagem, UUID usuarioId) {
        log.info("===== CRIANDO RIFA COM IMAGEM =====");
        log.info("Usuário: {}", usuarioId);
        log.info("Título: {}", request.getTitulo());
        log.info("Imagem recebida: {}", imagem != null ? imagem.getOriginalFilename() : "NULL");
        log.info("Imagem vazia: {}", imagem != null ? imagem.isEmpty() : "N/A");

        // 1️⃣ Criar a rifa primeiro
        Rifa rifa = rifaMapper.toEntity(request, usuarioId);
        Rifa rifaCriada = criar(rifa);

        log.info("✅ Rifa criada com ID: {}", rifaCriada.getId());

        // 2️⃣ Upload da imagem
        if (imagem != null && !imagem.isEmpty()) {
            try {
                log.info("🔄 Iniciando upload da imagem...");
                log.info("Tamanho: {} bytes", imagem.getSize());
                log.info("Content-Type: {}", imagem.getContentType());

                ImagemUploadResponse uploadResponse = imagemService.uploadImagemRifa(
                        imagem,
                        rifaCriada.getId().toString()
                );

                log.info("✅ Upload concluído. URL: {}", uploadResponse.getUrl());

                rifaCriada.setImagemUrl(uploadResponse.getUrl());
                rifaCriada = rifaRepository.save(rifaCriada);

                log.info("✅ Rifa atualizada com imagem no banco");

            } catch (Exception e) {
                log.error("❌ ERRO NO UPLOAD DA IMAGEM: ", e);
                log.error("Mensagem: {}", e.getMessage());
            }
        } else {
            log.warn("⚠️ Nenhuma imagem foi enviada ou imagem está vazia");
        }

        log.info("===== FIM CRIAÇÃO RIFA =====");
        return rifaCriada;
    }

    private void gerarNumeros(Rifa rifa) {
        log.info("Gerando {} números para rifa {}", rifa.getQuantidadeNumeros(), rifa.getId());

        List<Numero> numeros = new ArrayList<>();
        for (int i = 1; i <= rifa.getQuantidadeNumeros(); i++) {
            Numero numero = Numero.builder()
                    .rifaId(rifa.getId())
                    .numero(i)
                    .status(StatusNumero.DISPONIVEL)
                    .build();
            numeros.add(numero);
        }

        numeroRepository.saveAll(numeros);
        log.info("{} números gerados com sucesso", numeros.size());
    }

    private void validarQuantidadeNumeros(Integer quantidade) {
        if (quantidade < quantidadeMinima || quantidade > quantidadeMaxima) {
            throw new BusinessException(
                    String.format("Quantidade deve estar entre %d e %d", quantidadeMinima, quantidadeMaxima)
            );
        }

        if (quantidade % multiploDe != 0) {
            throw new BusinessException(
                    String.format("Quantidade deve ser múltiplo de %d", multiploDe)
            );
        }
    }

    @Transactional(readOnly = true)
    public Rifa buscarPorId(UUID id) {
        return rifaRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.rifa(id.toString()));
    }

    @Transactional(readOnly = true)
    public Page<Rifa> listarAtivas(Pageable pageable) {
        return rifaRepository.findByStatusOrderByDataCriacaoDesc(StatusRifa.ATIVA, pageable);
    }

    @Transactional(readOnly = true)
    public List<Rifa> listarPorUsuario(UUID usuarioId) {
        return rifaRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuarioId);
    }

    @Transactional
    public void cancelar(UUID id, UUID usuarioId) {
        log.info("Cancelando rifa: {} por usuário: {}", id, usuarioId);

        Rifa rifa = buscarPorId(id);

        if (!rifa.getUsuarioId().equals(usuarioId)) {
            throw new BusinessException("Você não pode cancelar esta rifa");
        }

        if (!rifa.podeSerCancelada()) {
            throw new BusinessException("Rifa não pode ser cancelada no status atual");
        }

        long vendidos = numeroRepository.countByRifaIdAndStatus(id, StatusNumero.VENDIDO);
        if (vendidos > 0) {
            throw new BusinessException("Não é possível cancelar rifa com números já vendidos");
        }

        rifa.setStatus(StatusRifa.CANCELADA);
        rifaRepository.save(rifa);

        log.info("Rifa cancelada com sucesso: {}", id);
    }

    @Transactional
    public void marcarComoCompleta(UUID rifaId) {
        log.info("Marcando rifa como completa: {}", rifaId);
        Rifa rifa = buscarPorId(rifaId);
        rifa.setStatus(StatusRifa.COMPLETA);
        rifaRepository.save(rifa);
    }

    @Transactional
    public void marcarComoSorteada(UUID rifaId, Integer numeroVencedor, UUID compradorVencedorId) {
        log.info("Marcando rifa como sorteada: {}", rifaId);
        Rifa rifa = buscarPorId(rifaId);
        rifa.setStatus(StatusRifa.SORTEADA);
        rifa.setNumeroVencedor(numeroVencedor);
        rifa.setCompradorVencedorId(compradorVencedorId);
        rifa.setDataSorteio(LocalDateTime.now());
        rifaRepository.save(rifa);
        log.info("Rifa sorteada - Número vencedor: {}", numeroVencedor);
    }

    @Transactional(readOnly = true)
    public boolean vendeuTodosNumeros(UUID rifaId) {
        long disponiveis = numeroRepository.countByRifaIdAndStatus(rifaId, StatusNumero.DISPONIVEL);
        long reservados = numeroRepository.countByRifaIdAndStatus(rifaId, StatusNumero.RESERVADO);
        return (disponiveis + reservados) == 0;
    }
}