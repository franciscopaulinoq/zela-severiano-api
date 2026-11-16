package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.DisponibilidadeFotoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.DisponibilidadeFotoDTO.SituacaoFoto;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.EstatisticasRelatosDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.EstatisticasRelatosDTO.TotalPorCategoria;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.HistoricoStatusDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoCriadoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoDetalheDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoGestaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoListagemDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoPublicoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.LimiteRelatosPendentesException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.HistoricoStatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.LimitesDoMunicipio;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.HistoricoStatusRelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.storage.FileStorage;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RelatoService {

  public static final int LIMITE_RELATOS_PENDENTES = 5;

  private static final Set<String> TIPOS_DE_FOTO_PERMITIDOS =
      Set.of("image/jpeg", "image/png", "image/webp");

  private final RelatoRepository relatoRepository;
  private final PerfilRepository perfilRepository;
  private final CategoriaRepository categoriaRepository;
  private final FileStorage fileStorage;
  private final HistoricoStatusRelatoRepository historicoRepository;

  @Transactional
  public RelatoCriadoDTO registrar(
      UUID usuarioId,
      RelatoRegistroDTO dto,
      byte[] fileBytes,
      String originalFilename,
      String contentType) {
    if (!LimitesDoMunicipio.contem(dto.getLatitude(), dto.getLongitude())) {
      throw new IllegalArgumentException(
          "A localização informada fica fora do município de Doutor Severiano.");
    }

    boolean temFoto = fileBytes != null;
    String tipoFoto = temFoto ? validarFoto(fileBytes, contentType) : null;

    Perfil perfil =
        perfilRepository
            .findByUsuarioId(usuarioId)
            .orElseThrow(
                () -> new RecursoNaoEncontradoException("Perfil do usuário não encontrado."));

    perfilRepository.bloquear(perfil.getId());
    long pendentes =
        relatoRepository.countByPerfilIdAndStatus(perfil.getId(), StatusRelato.PENDENTE);
    if (pendentes >= LIMITE_RELATOS_PENDENTES) {
      throw new LimiteRelatosPendentesException(
          "Você já tem %d relatos aguardando avaliação. Aguarde a avaliação de algum deles para registrar um novo."
              .formatted(LIMITE_RELATOS_PENDENTES));
    }

    Categoria categoria =
        categoriaRepository
            .findById(dto.getCategoriaId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada."));

    String urlFoto = null;
    if (temFoto) {
      String fileName = UUID.randomUUID() + extensaoDaFoto(tipoFoto, originalFilename);
      urlFoto = fileStorage.upload(fileBytes, fileName, tipoFoto);
    }

    Relato relato =
        Relato.builder()
            .perfil(perfil)
            .categoria(categoria)
            .status(StatusRelato.PENDENTE)
            .descricao(dto.getDescricao())
            .urlFoto(urlFoto)
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .build();

    return toCriadoDTO(relatoRepository.save(relato));
  }

  @Transactional(readOnly = true)
  public Page<RelatoListagemDTO> visualizarMeus(
      UUID usuarioId, FiltroRelatos filtro, Pageable pageable) {
    Perfil perfil =
        perfilRepository
            .findByUsuarioId(usuarioId)
            .orElseThrow(
                () -> new RecursoNaoEncontradoException("Perfil do usuário não encontrado."));

    return relatoRepository
        .findAllByPerfilId(perfil.getId(), filtro, pageable)
        .map(this::toListagemDTO);
  }

  @Transactional(readOnly = true)
  public RelatoDetalheDTO obterDetalhe(UUID usuarioId, Long relatoId) {
    Perfil perfil =
        perfilRepository
            .findByUsuarioId(usuarioId)
            .orElseThrow(
                () -> new RecursoNaoEncontradoException("Perfil do usuário não encontrado."));

    Relato relato =
        relatoRepository
            .findByPerfilIdAndId(perfil.getId(), relatoId)
            .orElseThrow(() ->
                new RecursoNaoEncontradoException(
                    "Relato não encontrado ou não pertence a este usuário."));

    return toDetalheDTO(relato);
  }

  @Transactional(readOnly = true)
  public Page<RelatoGestaoDTO> listarTodos(FiltroRelatos filtro, Pageable pageable) {
    return relatoRepository.findAll(filtro, pageable).map(this::toGestaoDTO);
  }

  @Transactional(readOnly = true)
  public Page<DisponibilidadeFotoDTO> verificarDisponibilidadeFotos(
      FiltroRelatos filtro, Pageable pageable) {
    return relatoRepository
        .findAll(filtro, pageable)
        .map(relato -> {
          if (relato.getUrlFoto() == null) {
            return new DisponibilidadeFotoDTO(relato.getId(), SituacaoFoto.SEM_FOTO);
          }
          boolean disponivel = fileStorage.existe(relato.getUrlFoto());
          return new DisponibilidadeFotoDTO(
              relato.getId(), disponivel ? SituacaoFoto.DISPONIVEL : SituacaoFoto.INDISPONIVEL);
        });
  }

  @Transactional(readOnly = true)
  public Page<RelatoPublicoDTO> listarPublicos(Integer categoriaId, Pageable pageable) {
    return relatoRepository
        .findAll(new FiltroRelatos(null, categoriaId), pageable)
        .map(this::toPublicoDTO);
  }

  @Transactional
  public RelatoCriadoDTO atualizarStatus(
      Long relatoId, StatusRelato novoStatus, String observacao) {
    Relato relato =
        relatoRepository
            .findByIdParaAtualizacao(relatoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Relato não encontrado."));

    StatusRelato statusAnterior = relato.getStatus();
    relato.atualizarStatus(novoStatus, observacao);

    Relato salvo = relatoRepository.save(relato);
    registrarHistorico(salvo, statusAnterior, observacao);
    return toCriadoDTO(salvo);
  }

  @Transactional(readOnly = true)
  public List<HistoricoStatusDTO> obterHistorico(UUID usuarioId, Long relatoId) {
    Perfil perfil =
        perfilRepository
            .findByUsuarioId(usuarioId)
            .orElseThrow(
                () -> new RecursoNaoEncontradoException("Perfil do usuário não encontrado."));

    relatoRepository
        .findByPerfilIdAndId(perfil.getId(), relatoId)
        .orElseThrow(() ->
            new RecursoNaoEncontradoException(
                "Relato não encontrado ou não pertence a este usuário."));

    return listarHistorico(relatoId);
  }

  @Transactional(readOnly = true)
  public EstatisticasRelatosDTO obterEstatisticas() {
    Map<StatusRelato, Long> porStatus = new EnumMap<>(StatusRelato.class);
    for (StatusRelato status : StatusRelato.values()) {
      porStatus.put(status, 0L);
    }
    relatoRepository
        .contarPorStatus()
        .forEach(total -> porStatus.put(total.status(), total.total()));

    List<TotalPorCategoria> porCategoria =
        relatoRepository.contarPorCategoria().stream()
            .map(total ->
                new TotalPorCategoria(total.categoriaId(), total.categoriaNome(), total.total()))
            .toList();

    return new EstatisticasRelatosDTO(relatoRepository.count(), porStatus, porCategoria);
  }

  @Transactional(readOnly = true)
  public List<HistoricoStatusDTO> obterHistoricoGestao(Long relatoId) {
    relatoRepository
        .findById(relatoId)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Relato não encontrado."));

    return listarHistorico(relatoId);
  }

  @Transactional
  public void cancelar(UUID usuarioId, Long relatoId) {
    Perfil perfil =
        perfilRepository
            .findByUsuarioId(usuarioId)
            .orElseThrow(
                () -> new RecursoNaoEncontradoException("Perfil do usuário não encontrado."));

    Relato relato =
        relatoRepository
            .findByPerfilIdAndIdParaAtualizacao(perfil.getId(), relatoId)
            .orElseThrow(() ->
                new RecursoNaoEncontradoException(
                    "Relato não encontrado ou não pertence a este usuário."));

    StatusRelato statusAnterior = relato.getStatus();
    relato.cancelar();

    Relato salvo = relatoRepository.save(relato);
    registrarHistorico(salvo, statusAnterior, null);
  }

  private void registrarHistorico(Relato relato, StatusRelato statusAnterior, String observacao) {
    historicoRepository.save(
        HistoricoStatusRelato.builder()
            .relatoId(relato.getId())
            .statusAnterior(statusAnterior)
            .statusNovo(relato.getStatus())
            .observacao(observacao)
            .alteradoEm(relato.getAtualizadoEm())
            .build());
  }

  private List<HistoricoStatusDTO> listarHistorico(Long relatoId) {
    return historicoRepository.findAllByRelatoId(relatoId).stream()
        .map(entrada ->
            HistoricoStatusDTO.builder()
                .statusAnterior(entrada.getStatusAnterior())
                .statusNovo(entrada.getStatusNovo())
                .observacao(entrada.getObservacao())
                .alteradoEm(entrada.getAlteradoEm())
                .build())
        .toList();
  }

  private String validarFoto(byte[] fileBytes, String contentType) {
    if (fileBytes.length > 10 * 1024 * 1024) {
      throw new IllegalArgumentException("A foto deve ter no máximo 10 MB.");
    }
    if (contentType == null) {
      throw new IllegalArgumentException(
          "Tipo de arquivo não permitido para a foto. Envie uma imagem JPEG, PNG ou WebP.");
    }

    String tipo = contentType.toLowerCase(Locale.ROOT);
    if (!TIPOS_DE_FOTO_PERMITIDOS.contains(tipo)) {
      throw new IllegalArgumentException(
          "Tipo de arquivo não permitido para a foto. Envie uma imagem JPEG, PNG ou WebP.");
    }

    boolean conteudoValido =
        switch (tipo) {
          case "image/jpeg" -> assinaturaConfere(fileBytes, 0, 0xFF, 0xD8, 0xFF);
          case "image/png" ->
              assinaturaConfere(fileBytes, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
          case "image/webp" ->
              assinaturaConfere(fileBytes, 0, 0x52, 0x49, 0x46, 0x46)
                  && assinaturaConfere(fileBytes, 8, 0x57, 0x45, 0x42, 0x50);
          default -> false;
        };
    if (!conteudoValido) {
      throw new IllegalArgumentException(
          "O conteúdo do arquivo não corresponde a uma imagem JPEG, PNG ou WebP válida.");
    }
    return tipo;
  }

  private String extensaoDaFoto(String tipo, String originalFilename) {
    if ("image/png".equals(tipo)) {
      return ".png";
    }
    if ("image/webp".equals(tipo)) {
      return ".webp";
    }
    return originalFilename != null && originalFilename.toLowerCase(Locale.ROOT).endsWith(".jpeg")
        ? ".jpeg"
        : ".jpg";
  }

  private static boolean assinaturaConfere(byte[] bytes, int offset, int... assinatura) {
    if (bytes.length < offset + assinatura.length) {
      return false;
    }
    for (int i = 0; i < assinatura.length; i++) {
      if ((bytes[offset + i] & 0xFF) != assinatura[i]) {
        return false;
      }
    }
    return true;
  }

  private RelatoCriadoDTO toCriadoDTO(Relato relato) {
    return RelatoCriadoDTO.builder()
        .id(relato.getId())
        .status(relato.getStatus())
        .urlFoto(relato.getUrlFoto())
        .criadoEm(relato.getCriadoEm())
        .build();
  }

  private RelatoListagemDTO toListagemDTO(Relato relato) {
    return RelatoListagemDTO.builder()
        .id(relato.getId())
        .categoriaNome(relato.getCategoria().getNome())
        .categoriaIconeNome(relato.getCategoria().getIconeNome())
        .status(relato.getStatus())
        .criadoEm(relato.getCriadoEm())
        .build();
  }

  private RelatoDetalheDTO toDetalheDTO(Relato relato) {
    return RelatoDetalheDTO.builder()
        .id(relato.getId())
        .urlFoto(relato.getUrlFoto())
        .status(relato.getStatus())
        .criadoEm(relato.getCriadoEm())
        .categoriaNome(relato.getCategoria().getNome())
        .latitude(relato.getLatitude())
        .longitude(relato.getLongitude())
        .descricao(relato.getDescricao())
        .build();
  }

  private RelatoPublicoDTO toPublicoDTO(Relato relato) {
    return RelatoPublicoDTO.builder()
        .categoriaNome(relato.getCategoria().getNome())
        .categoriaIconeNome(relato.getCategoria().getIconeNome())
        .status(relato.getStatus())
        .criadoEm(relato.getCriadoEm())
        .build();
  }

  private RelatoGestaoDTO toGestaoDTO(Relato relato) {
    return RelatoGestaoDTO.builder()
        .id(relato.getId())
        .cidadaoNome(relato.getPerfil().getNomeCompleto())
        .categoriaNome(relato.getCategoria().getNome())
        .status(relato.getStatus())
        .descricao(relato.getDescricao())
        .observacaoResolucao(relato.getObservacaoResolucao())
        .criadoEm(relato.getCriadoEm())
        .atualizadoEm(relato.getAtualizadoEm())
        .build();
  }
}
