package io.github.franciscopaulinoq.zelaseveriano.application.service;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.CategoriaRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.ImportacaoCategoriaErroDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.ImportacaoCategoriaResultadoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.CategoriaEmUsoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.exception.RecursoNaoEncontradoException;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Categoria;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.CategoriaRepository;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

  private final CategoriaRepository categoriaRepository;
  private final RelatoRepository relatoRepository;

  @Transactional(readOnly = true)
  public List<CategoriaDTO> listar() {
    return categoriaRepository.findAll().stream().map(this::toDTO).toList();
  }

  @Transactional
  public CategoriaDTO criar(CategoriaRegistroDTO dados) {
    Categoria categoria =
        Categoria.builder().nome(dados.getNome()).iconeNome(dados.getIconeNome()).build();

    return toDTO(categoriaRepository.save(categoria));
  }

  @Transactional
  public ImportacaoCategoriaResultadoDTO importar(Reader conteudo) throws IOException {
    List<ImportacaoCategoriaErroDTO> erros = new ArrayList<>();
    List<LinhaCategoria> linhas = lerLinhas(conteudo, erros);
    validarDuplicidades(linhas, erros);

    Set<String> nomesExistentes =
        listar().stream()
            .map(categoria -> normalizarNome(categoria.getNome()))
            .collect(Collectors.toSet());
    for (LinhaCategoria linha : linhas) {
      if (nomesExistentes.contains(normalizarNome(linha.nome()))) {
        erros.add(new ImportacaoCategoriaErroDTO(linha.numeroLinha(), "Nome já cadastrado."));
      }
    }

    if (!erros.isEmpty()) {
      return new ImportacaoCategoriaResultadoDTO(0, List.copyOf(erros));
    }
    for (LinhaCategoria linha : linhas) {
      criar(new CategoriaRegistroDTO(linha.nome(), linha.iconeNome()));
    }
    return new ImportacaoCategoriaResultadoDTO(linhas.size(), List.of());
  }

  @Transactional
  public CategoriaDTO atualizar(Integer categoriaId, CategoriaRegistroDTO dados) {
    Categoria categoria =
        categoriaRepository
            .findById(categoriaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada."));

    categoria.atualizar(dados.getNome(), dados.getIconeNome());

    return toDTO(categoriaRepository.save(categoria));
  }

  @Transactional
  public void remover(Integer categoriaId) {
    if (!categoriaRepository.existsById(categoriaId)) {
      throw new RecursoNaoEncontradoException("Categoria não encontrada.");
    }

    if (relatoRepository.existsByCategoriaId(categoriaId)) {
      throw new CategoriaEmUsoException(
          "A categoria possui relatos vinculados e não pode ser removida.");
    }

    categoriaRepository.deleteById(categoriaId);
  }

  static void validarDuplicidades(
      List<LinhaCategoria> linhas, List<ImportacaoCategoriaErroDTO> erros) {
    for (LinhaCategoria linha : linhas) {
      String chave = normalizarNome(linha.nome());
      long ocorrencias =
          linhas.stream().filter(outra -> normalizarNome(outra.nome()).equals(chave)).count();
      if (ocorrencias > 1) {
        LinhaCategoria primeira =
            linhas.stream()
                .filter(outra -> normalizarNome(outra.nome()).equals(chave))
                .findFirst()
                .orElseThrow();
        if (primeira.numeroLinha() != linha.numeroLinha()) {
          erros.add(
              new ImportacaoCategoriaErroDTO(
                  linha.numeroLinha(),
                  "Nome duplicado no arquivo; primeira ocorrência na linha %d."
                      .formatted(primeira.numeroLinha())));
        }
      }
    }
  }

  private static List<LinhaCategoria> lerLinhas(
      Reader conteudo, List<ImportacaoCategoriaErroDTO> erros) throws IOException {
    BufferedReader leitor = new BufferedReader(conteudo);
    List<LinhaCategoria> linhas = new ArrayList<>();
    String cabecalho = leitor.readLine();
    if (cabecalho == null || !cabecalhoValido(cabecalho)) {
      erros.add(new ImportacaoCategoriaErroDTO(1, "Cabeçalho esperado: nome,icone."));
      return linhas;
    }

    int numeroLinha = 1;
    while (true) {
      String texto = leitor.readLine();
      if (texto == null) {
        break;
      }
      numeroLinha++;
      if (texto.isBlank()) {
        continue;
      }

      List<String> campos;
      try {
        campos = parseCsv(texto);
      } catch (IllegalArgumentException exception) {
        erros.add(new ImportacaoCategoriaErroDTO(numeroLinha, "Formato CSV inválido."));
        continue;
      }
      if (campos.size() != 2) {
        erros.add(
            new ImportacaoCategoriaErroDTO(
                numeroLinha, "Informe exatamente as colunas nome e ícone."));
        continue;
      }

      String nome = campos.getFirst().strip();
      String icone = campos.get(1).strip();
      if (nome.isEmpty() || icone.isEmpty()) {
        erros.add(new ImportacaoCategoriaErroDTO(numeroLinha, "Nome e ícone são obrigatórios."));
        continue;
      }
      linhas.add(new LinhaCategoria(numeroLinha, nome, icone));
    }
    return linhas;
  }

  private static boolean cabecalhoValido(String texto) {
    try {
      List<String> campos = parseCsv(texto);
      return campos.size() == 2
          && "nome".equalsIgnoreCase(campos.getFirst().replace("\uFEFF", "").strip())
          && "icone".equalsIgnoreCase(campos.get(1).strip());
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  private static List<String> parseCsv(String texto) {
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
        campos.add(campo.toString());
        campo.setLength(0);
      } else {
        campo.append(caractere);
      }
    }
    if (entreAspas) {
      throw new IllegalArgumentException("Aspas não encerradas");
    }
    campos.add(campo.toString());
    return campos;
  }

  private static String normalizarNome(String nome) {
    return nome.trim().toLowerCase(Locale.ROOT);
  }

  private CategoriaDTO toDTO(Categoria categoria) {
    return CategoriaDTO.builder()
        .id(categoria.getId())
        .nome(categoria.getNome())
        .iconeNome(categoria.getIconeNome())
        .build();
  }

  record LinhaCategoria(int numeroLinha, String nome, String iconeNome) {}
}
