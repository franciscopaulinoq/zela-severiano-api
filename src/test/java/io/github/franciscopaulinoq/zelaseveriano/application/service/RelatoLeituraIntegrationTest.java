package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoDetalheDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoGestaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoListagemDTO;
import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.FiltroRelatos;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class RelatoLeituraIntegrationTest {

  @Autowired private RelatoService relatoService;

  @Autowired private CidadaosDeTeste cidadaos;

  @Test
  void fluxosDeLeituraCarregamCidadaoECategoriaSemErro() {
    Cidadao cidadao = cidadaos.cadastrar("Joana Pereira");
    var criado =
        relatoService.registrar(
            cidadao.usuarioId(),
            new RelatoRegistroDTO(1, "Lâmpada queimada", -6.08, -38.38),
            null,
            null,
            null);

    List<RelatoListagemDTO> meus =
        relatoService
            .visualizarMeus(cidadao.usuarioId(), FiltroRelatos.semFiltro(), PageRequest.of(0, 10))
            .getContent();
    RelatoDetalheDTO detalhe = relatoService.obterDetalhe(cidadao.usuarioId(), criado.getId());
    List<RelatoGestaoDTO> todos =
        relatoService.listarTodos(FiltroRelatos.semFiltro(), PageRequest.of(0, 10)).getContent();
    var atualizado = relatoService.atualizarStatus(criado.getId(), StatusRelato.EM_ANALISE, null);

    assertThat(meus)
        .singleElement()
        .satisfies(item -> {
          assertThat(item.getId()).isEqualTo(criado.getId());
          assertThat(item.getCategoriaNome()).isEqualTo("Iluminação");
        });
    assertThat(detalhe.getCategoriaNome()).isEqualTo("Iluminação");
    assertThat(detalhe.getDescricao()).isEqualTo("Lâmpada queimada");
    assertThat(todos)
        .filteredOn(item -> item.getId().equals(criado.getId()))
        .singleElement()
        .satisfies(item -> {
          assertThat(item.getCidadaoNome()).isEqualTo("Joana Pereira");
          assertThat(item.getBairro()).isEqualTo("Centro");
        });
    assertThat(atualizado.getStatus()).isEqualTo(StatusRelato.EM_ANALISE);
  }
}
