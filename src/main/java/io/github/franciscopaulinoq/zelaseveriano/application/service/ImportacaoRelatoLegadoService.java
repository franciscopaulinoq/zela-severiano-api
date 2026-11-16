package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.ImportacaoRelatoResultadoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.LimitesDoMunicipio;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Perfil;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.PerfilRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImportacaoRelatoLegadoService {

  private static final String CABECALHO =
      "identificador_externo,cpf,categoria_id,descricao,latitude,longitude,status,criado_em";

  private final RelatoRepository relatoRepository;
  private final PerfilRepository perfilRepository;
  private final CategoriaRepository categoriaRepository;

  @Transactional
  public ImportacaoRelatoResultadoDTO importar(MultipartFile arquivo) {
    List<String> erros = new ArrayList<>();
    Set<String> identificadores = new HashSet<>();
    try {
      List<LinhaLegado> linhas = lerLinhas(arquivo, erros);
      List<Relato> relatos = processarLote(linhas, carregarCategorias(), erros, identificadores);
      if (!erros.isEmpty()) {
        throw new IllegalArgumentException(String.join(" ", erros));
      }
      relatoRepository.saveAll(relatos);
      return new ImportacaoRelatoResultadoDTO(relatos.size());
    } catch (IOException exception) {
      throw new IllegalArgumentException("Não foi possível ler o arquivo CSV.", exception);
    }
  }

  private List<LinhaLegado> lerLinhas(MultipartFile arquivo, List<String> erros)
      throws IOException {
    String conteudo = new String(arquivo.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    List<LinhaLegado> linhas = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new StringReader(conteudo))) {
      validarCabecalho(reader.readLine());
      String texto = reader.readLine();
      int numero = 1;
      while (texto != null) {
        numero++;
        if (!texto.isBlank()) {
          LinhaLegado linha = lerLinha(numero, texto, erros);
          if (linha != null) {
            linhas.add(linha);
          }
        }
        texto = reader.readLine();
      }
    }
    return linhas;
  }

  private List<Relato> processarLote(
      List<LinhaLegado> linhas,
      Map<Integer, Categoria> categorias,
      List<String> erros,
      Set<String> identificadores) {
    Map<String, Perfil> perfis = carregarPerfis(linhas);
    List<Relato> relatos = new ArrayList<>();
    for (LinhaLegado linha : linhas) {
      String erro = validarLinha(linha, perfis, categorias, identificadores);
      if (erro != null) {
        erros.add("Linha %d: %s".formatted(linha.numero(), erro));
      } else {
        relatos.add(criarRelato(linha, perfis, categorias));
      }
    }
    return relatos;
  }

  private String validarLinha(
      LinhaLegado linha,
      Map<String, Perfil> perfis,
      Map<Integer, Categoria> categorias,
      Set<String> identificadores) {
    if (linha.identificadorExterno().isBlank() || linha.identificadorExterno().length() > 100) {
      return "identificador externo obrigatório com no máximo 100 caracteres.";
    }
    if (!identificadores.add(linha.identificadorExterno())
        || relatoRepository.findByIdentificadorExterno(linha.identificadorExterno()).isPresent()) {
      return "identificador externo duplicado.";
    }
    if (!perfis.containsKey(linha.cpf())) {
      return "CPF sem perfil cadastrado.";
    }
    Integer categoriaId = inteiro(linha.categoriaId());
    if (categoriaId == null || !categorias.containsKey(categoriaId)) {
      return "categoria inexistente.";
    }
    if (linha.descricao().isBlank()) {
      return "descrição obrigatória.";
    }
    Double latitude = decimal(linha.latitude());
    Double longitude = decimal(linha.longitude());
    if (latitude == null || longitude == null) {
      return "coordenadas inválidas.";
    }
    if (!LimitesDoMunicipio.contem(latitude, longitude)) {
      return "coordenadas fora do município.";
    }
    if (status(linha.status()) == null) {
      return "status desconhecido.";
    }
    if (data(linha.criadoEm()) == null) {
      return "data de criação inválida; use ISO-8601 com fuso.";
    }
    return null;
  }

  private Relato criarRelato(
      LinhaLegado linha, Map<String, Perfil> perfis, Map<Integer, Categoria> categorias) {
    return Relato.builder()
        .identificadorExterno(linha.identificadorExterno())
        .perfil(perfis.get(linha.cpf()))
        .categoria(categorias.get(inteiro(linha.categoriaId())))
        .descricao(linha.descricao())
        .latitude(decimal(linha.latitude()))
        .longitude(decimal(linha.longitude()))
        .status(status(linha.status()))
        .criadoEm(data(linha.criadoEm()))
        .build();
  }

  private Map<Integer, Categoria> carregarCategorias() {
    return categoriaRepository.findAll().stream()
        .collect(Collectors.toMap(Categoria::getId, Function.identity()));
  }

  private Map<String, Perfil> carregarPerfis(List<LinhaLegado> linhas) {
    Set<String> cpfs = linhas.stream().map(LinhaLegado::cpf).collect(Collectors.toSet());
    if (cpfs.isEmpty()) {
      return Map.of();
    }
    return perfilRepository.findAllByUsuarioCpfIn(cpfs).stream()
        .collect(Collectors.toMap(perfil -> perfil.getUsuario().getCpf(), Function.identity()));
  }

  private LinhaLegado lerLinha(int numero, String texto, List<String> erros) {
    List<String> campos = separarCampos(texto);
    if (campos.size() != 8) {
      erros.add("Linha %d: esperadas 8 colunas.".formatted(numero));
      return null;
    }
    return new LinhaLegado(
        numero,
        campos.get(0),
        campos.get(1),
        campos.get(2),
        campos.get(3),
        campos.get(4),
        campos.get(5),
        campos.get(6),
        campos.get(7));
  }

  private List<String> separarCampos(String texto) {
    List<String> campos = new ArrayList<>();
    StringBuilder campo = new StringBuilder();
    boolean entreAspas = false;
    for (int i = 0; i < texto.length(); i++) {
      char caractere = texto.charAt(i);
      if (caractere == '"') {
        if (entreAspas && i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
          campo.append('"');
          i++;
        } else {
          entreAspas = !entreAspas;
        }
      } else if (caractere == ',' && !entreAspas) {
        campos.add(campo.toString().strip());
        campo.setLength(0);
      } else {
        campo.append(caractere);
      }
    }
    if (entreAspas) {
      return List.of();
    }
    campos.add(campo.toString().strip());
    return campos;
  }

  private void validarCabecalho(String cabecalho) {
    if (cabecalho == null || !CABECALHO.equals(cabecalho.strip())) {
      throw new IllegalArgumentException("Cabeçalho inválido. Use: " + CABECALHO);
    }
  }

  private Integer inteiro(String valor) {
    try {
      return Integer.valueOf(valor);
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private Double decimal(String valor) {
    try {
      double numero = Double.parseDouble(valor);
      return Double.isFinite(numero) ? numero : null;
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private StatusRelato status(String valor) {
    try {
      return StatusRelato.valueOf(valor.strip().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  private OffsetDateTime data(String valor) {
    try {
      return OffsetDateTime.parse(valor);
    } catch (DateTimeParseException exception) {
      return null;
    }
  }

  private record LinhaLegado(
      int numero,
      String identificadorExterno,
      String cpf,
      String categoriaId,
      String descricao,
      String latitude,
      String longitude,
      String status,
      String criadoEm) {}
}
