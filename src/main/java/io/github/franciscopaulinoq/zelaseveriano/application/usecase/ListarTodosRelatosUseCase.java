package io.github.franciscopaulinoq.zelaseveriano.application.usecase;

import io.github.franciscopaulinoq.zelaseveriano.application.dto.RelatoGestaoDTO;
import io.github.franciscopaulinoq.zelaseveriano.domain.model.Relato;
import io.github.franciscopaulinoq.zelaseveriano.domain.repository.RelatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarTodosRelatosUseCase {

    private final RelatoRepository relatoRepository;

    @Transactional(readOnly = true)
    public List<RelatoGestaoDTO> execute() {
        return relatoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    private RelatoGestaoDTO toDTO(Relato relato) {
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
