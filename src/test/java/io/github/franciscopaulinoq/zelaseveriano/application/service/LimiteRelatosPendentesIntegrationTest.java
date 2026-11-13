package io.github.franciscopaulinoq.zelaseveriano.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoRegistroDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.enums.StatusRelato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste;
import io.github.franciscopaulinoq.zelaseveriano.support.CidadaosDeTeste.Cidadao;
import io.github.franciscopaulinoq.zelaseveriano.support.IntegrationTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@IntegrationTest
@Import(CidadaosDeTeste.class)
class LimiteRelatosPendentesIntegrationTest {

    @Autowired
    private RelatoService relatoService;

    @Autowired
    private RelatoRepository relatoRepository;

    @Autowired
    private CidadaosDeTeste cidadaos;

    @Test
    void quandoUmPendenteEntraEmAnaliseOCidadaoVoltaAPoderRegistrar() {
        Cidadao cidadao = cidadaos.cadastrar("Pedro Alves");
        List<Relato> pendentes = new ArrayList<>();
        for (int i = 0; i < RelatoService.LIMITE_RELATOS_PENDENTES; i++) {
            pendentes.add(registrar(cidadao));
        }

        relatoService.atualizarStatus(pendentes.getFirst().getId(), StatusRelato.EM_ANALISE, null);
        Relato novo = registrar(cidadao);

        assertThat(novo.getStatus()).isEqualTo(StatusRelato.PENDENTE);
        assertThat(relatoRepository.countByPerfilIdAndStatus(cidadao.perfilId(), StatusRelato.PENDENTE))
                .isEqualTo(RelatoService.LIMITE_RELATOS_PENDENTES);
    }

    private Relato registrar(Cidadao cidadao) {
        return relatoService.registrar(
                cidadao.usuarioId(), new RelatoRegistroDTO(4, "Buraco na rua", -6.08, -38.38), null, null, null);
    }
}
